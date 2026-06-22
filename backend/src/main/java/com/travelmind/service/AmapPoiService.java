package com.travelmind.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmind.config.AppProperties;
import com.travelmind.model.Attraction;
import com.travelmind.model.Hotel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AmapPoiService {
    private final AppProperties properties;
    private final HotelPriceService hotelPriceService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final RestClient restClient = RestClient.create();
    private static final TypeReference<List<Attraction>> ATTRACTION_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<Hotel>> HOTEL_LIST = new TypeReference<>() {
    };

    public record PoiSummary(String name, String city, String address, String rating, String type) {
        public String line() {
            return "%s｜%s｜%s｜评分%s｜%s".formatted(name, city, address, rating, type);
        }
    }

    public List<Attraction> searchAttractions(String keyword, String city, int limit, String requestKey) {
        var key = resolveKey(requestKey);
        var size = Math.min(Math.max(limit, 1), 60);
        var cacheKey = cacheKey("attractions", keyword, city, size);
        var cached = readCache(cacheKey, ATTRACTION_LIST);
        if (cached != null) return cached;

        var merged = new LinkedHashMap<Long, Map<String, Object>>();
        for (var query : attractionQueries(keyword, city)) {
            safeSearchRaw(query.keyword(), query.city(), null, Math.min(25, Math.max(size, 15)), key)
                    .forEach(poi -> merged.putIfAbsent(stableId(poi), poi));
            if (merged.size() >= size * 3) break;
        }
        var list = merged.values().stream()
                .filter(this::isTravelPoi)
                .sorted(Comparator.comparingInt(this::attractionRank).reversed()
                        .thenComparing((Map<String, Object> poi) -> decimal(businessValue(poi, "rating")).orElse(BigDecimal.ZERO), Comparator.reverseOrder()))
                .limit(size)
                .map(this::toAttraction)
                .toList();
        writeCache(cacheKey, list, Duration.ofMinutes(20));
        return list;
    }

    public List<Hotel> searchHotels(String keyword, String city, int limit, String requestKey) {
        var key = resolveKey(requestKey);
        var size = Math.min(Math.max(limit, 1), 40);
        var cacheKey = cacheKey("hotels", keyword, city, size);
        var cached = readCache(cacheKey, HOTEL_LIST);
        if (cached != null) return cached;

        var keywords = hotelKeywords(keyword, city);
        var merged = new LinkedHashMap<Long, Map<String, Object>>();
        for (var word : keywords) {
            searchRaw(word, city, "100000", Math.min(25, Math.max(size, 12)), key).forEach(poi -> merged.putIfAbsent(stableId(poi), poi));
            if (merged.size() >= size * 3) break;
        }
        var list = merged.values().stream()
                .filter(this::isRegularHotel)
                .sorted(Comparator.comparingInt(this::hotelRank).reversed()
                        .thenComparing((Map<String, Object> poi) -> decimal(businessValue(poi, "rating")).orElse(BigDecimal.ZERO), Comparator.reverseOrder()))
                .limit(size)
                .map(this::toHotel)
                .toList();
        writeCache(cacheKey, list, Duration.ofMinutes(20));
        return list;
    }

    public List<PoiSummary> searchSummaries(String keyword, String city, int limit) {
        var key = configuredKey().orElse("");
        if (key.isBlank()) return List.of();
        try {
            return searchRaw(keyword, city, null, limit, key).stream()
                    .map(poi -> new PoiSummary(text(poi, "name"), text(poi, "cityname"), text(poi, "address"), ratingText(poi), text(poi, "type")))
                    .toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchRaw(String keyword, String city, String types, int limit, String key) {
        var body = restClient.get()
                .uri(uri -> {
                    var builder = uri.scheme("https")
                            .host("restapi.amap.com")
                            .path("/v5/place/text")
                            .queryParam("key", key)
                            .queryParam("keywords", keyword)
                            .queryParam("region", city == null ? "" : city)
                            .queryParam("city_limit", city != null && !city.isBlank())
                            .queryParam("show_fields", "photos,photo,business")
                            .queryParam("page_size", Math.min(Math.max(limit, 1), 25));
                    if (types != null && !types.isBlank()) {
                        builder.queryParam("types", types);
                    }
                    return builder.build();
                })
                .retrieve()
                .body(Map.class);
        if (body == null || !"1".equals(String.valueOf(body.get("status")))) {
            throw new IllegalArgumentException(String.valueOf(body == null ? "高德联网搜索失败" : body.getOrDefault("info", "高德联网搜索失败")));
        }
        return (List<Map<String, Object>>) body.getOrDefault("pois", List.of());
    }

    private Attraction toAttraction(Map<String, Object> poi) {
        var item = new Attraction();
        item.setId(stableId(poi));
        item.setName(text(poi, "name"));
        item.setProvince(text(poi, "pname"));
        item.setCity(Optional.of(text(poi, "cityname")).filter(x -> !x.isBlank()).orElse(text(poi, "adname")));
        item.setDescription(Optional.of(text(poi, "address")).filter(x -> !x.isBlank()).orElse(text(poi, "type")));
        item.setOpenTime("联网结果");
        item.setBestSeason("全年");
        item.setTags(text(poi, "type"));
        item.setPrice(BigDecimal.ZERO);
        item.setScore(decimal(businessValue(poi, "rating")).orElse(BigDecimal.ZERO));
        setLocation(item, text(poi, "location"));
        item.setCoverImage(photo(poi).orElseGet(() -> staticMap(text(poi, "location"))));
        return item;
    }

    private Hotel toHotel(Map<String, Object> poi) {
        var item = new Hotel();
        item.setId(stableId(poi));
        item.setName(text(poi, "name"));
        item.setCity(Optional.of(text(poi, "cityname")).filter(x -> !x.isBlank()).orElse(text(poi, "adname")));
        item.setAddress(text(poi, "address"));
        item.setPrice(hotelPriceService.resolve(item.getName(), item.getCity(), decimal(businessValue(poi, "cost")).orElse(BigDecimal.ZERO)));
        item.setScore(decimal(businessValue(poi, "rating")).orElse(BigDecimal.ZERO));
        setLocation(item, text(poi, "location"));
        item.setCover(photo(poi).orElseGet(() -> staticMap(text(poi, "location"))));
        return item;
    }

    private void setLocation(Attraction item, String location) {
        var point = location.split(",");
        if (point.length == 2) {
            item.setLongitude(decimal(point[0]).orElse(null));
            item.setLatitude(decimal(point[1]).orElse(null));
        }
    }

    private void setLocation(Hotel item, String location) {
        var point = location.split(",");
        if (point.length == 2) {
            item.setLongitude(decimal(point[0]).orElse(null));
            item.setLatitude(decimal(point[1]).orElse(null));
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<String> photo(Map<String, Object> poi) {
        var value = poi.get("photos");
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            value = poi.get("photo");
        }
        if (!(value instanceof List<?> list)) {
            return Optional.empty();
        }
        return list.stream()
                .filter(Map.class::isInstance)
                .map(x -> (Map<String, Object>) x)
                .map(x -> String.valueOf(x.get("url")))
                .filter(x -> !x.isBlank() && !"null".equals(x))
                .findFirst();
    }

    private String staticMap(String location) {
        var key = configuredKey().orElse("");
        return location == null || location.isBlank() || key.isBlank()
                ? ""
                : "https://restapi.amap.com/v3/staticmap?location=" + location + "&zoom=13&size=600*320&markers=mid,,A:" + location + "&key=" + key;
    }

    private String resolveKey(String requestKey) {
        return Optional.ofNullable(requestKey)
                .filter(x -> !x.isBlank())
                .or(this::configuredKey)
                .orElseThrow(() -> new IllegalArgumentException("缺少高德 AMAP_KEY"));
    }

    private Optional<String> configuredKey() {
        return Optional.ofNullable(properties.amap()).map(AppProperties.Amap::key).filter(x -> !x.isBlank());
    }

    private String ratingText(Map<String, Object> poi) {
        return Optional.ofNullable(businessValue(poi, "rating")).filter(x -> !x.isBlank()).orElse("暂无");
    }

    private String businessValue(Map<String, Object> poi, String key) {
        var business = poi.get("business") instanceof Map<?, ?> map ? map : Map.of();
        return clean(business.get(key));
    }

    private Optional<BigDecimal> decimal(String value) {
        try {
            return value == null || value.isBlank() ? Optional.empty() : Optional.of(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String text(Map<String, Object> map, String key) {
        return clean(map.get(key));
    }

    private String clean(Object value) {
        return value == null || "[]".equals(String.valueOf(value)) || "null".equals(String.valueOf(value)) ? "" : String.valueOf(value);
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private List<Map<String, Object>> safeSearchRaw(String keyword, String city, String types, int limit, String key) {
        try {
            return searchRaw(keyword, city, types, limit, key);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private record PoiQuery(String keyword, String city) {
    }

    private List<PoiQuery> attractionQueries(String keyword, String city) {
        var base = blankToDefault(keyword, "旅游景点").trim();
        var cities = attractionCities(base, city);
        var words = attractionKeywords(base);
        var queries = new ArrayList<PoiQuery>();
        for (var targetCity : cities) {
            for (var word : words) {
                queries.add(new PoiQuery(word, targetCity));
            }
        }
        if ((city == null || city.isBlank()) && cities.isEmpty()) {
            words.forEach(word -> queries.add(new PoiQuery(word, "")));
        }
        return queries.stream().distinct().toList();
    }

    private List<String> attractionCities(String keyword, String city) {
        if (city != null && !city.isBlank()) return List.of(city.trim());
        if (containsAny(keyword, "海边", "海岛", "海滩", "沙滩", "看海", "赶海", "潜水")) {
            return List.of("三亚", "厦门", "青岛", "舟山", "大连", "威海", "北海", "深圳");
        }
        if (containsAny(keyword, "雪", "滑雪", "冰雪", "雪景")) {
            return List.of("哈尔滨", "长白山", "阿勒泰", "张家口", "沈阳");
        }
        if (containsAny(keyword, "古镇", "水乡", "江南")) {
            return List.of("苏州", "嘉兴", "湖州", "杭州", "黄山");
        }
        if (containsAny(keyword, "夜景", "夜市", "灯光")) {
            return List.of("上海", "重庆", "长沙", "广州", "香港");
        }
        if (containsAny(keyword, "山", "森林", "徒步", "峡谷")) {
            return List.of("张家界", "黄山", "桂林", "成都", "贵阳");
        }
        if (containsAny(keyword, "迪士尼", "亲子", "乐园", "游乐园")) {
            return List.of("上海", "香港", "东京", "大阪", "北京");
        }
        return List.of("");
    }

    private List<String> attractionKeywords(String keyword) {
        var words = new ArrayList<String>();
        words.add(keyword);
        if (containsAny(keyword, "海边", "海岛", "海滩", "沙滩", "看海", "赶海", "潜水")) {
            words.addAll(List.of("海滨浴场", "沙滩", "海滩", "海岛景区", "滨海公园", "赶海", "潜水"));
        } else if (containsAny(keyword, "雪", "滑雪", "冰雪", "雪景")) {
            words.addAll(List.of("滑雪场", "冰雪大世界", "雪景景区", "森林雪景", "温泉滑雪"));
        } else if (containsAny(keyword, "古镇", "水乡", "江南")) {
            words.addAll(List.of("古镇", "历史文化街区", "水乡景区", "古街", "老街"));
        } else if (containsAny(keyword, "夜景", "夜市", "灯光")) {
            words.addAll(List.of("夜景", "观景台", "步行街", "夜市", "灯光秀"));
        } else if (containsAny(keyword, "美食", "小吃")) {
            words.addAll(List.of("美食街", "夜市", "小吃街", "步行街"));
        } else if (containsAny(keyword, "山", "森林", "徒步", "峡谷")) {
            words.addAll(List.of("风景区", "森林公园", "国家公园", "山岳景区", "峡谷"));
        } else if (containsAny(keyword, "迪士尼", "亲子", "乐园", "游乐园")) {
            words.addAll(List.of("主题乐园", "游乐园", "迪士尼", "动物园", "海洋公园"));
        } else {
            words.addAll(List.of(keyword + " 景区", keyword + " 公园", keyword + " 打卡", keyword + " 旅游景点"));
        }
        words.add("热门景点");
        words.add("风景名胜");
        return words.stream().filter(x -> x != null && !x.isBlank()).distinct().toList();
    }

    private boolean isTravelPoi(Map<String, Object> poi) {
        var text = (text(poi, "name") + " " + text(poi, "type") + " " + text(poi, "address")).toLowerCase();
        if (text.isBlank()) return false;
        if (containsAny(text, "酒店", "宾馆", "停车场", "收费站", "公司", "写字楼", "小区", "住宅", "售楼", "维修", "银行", "医院", "学校")) {
            return false;
        }
        return attractionRank(poi) > 0 || containsAny(text, "风景", "景区", "公园", "博物馆", "海滩", "沙滩", "古镇", "乐园", "步行街", "夜市");
    }

    private int attractionRank(Map<String, Object> poi) {
        var text = (text(poi, "name") + " " + text(poi, "type") + " " + text(poi, "address")).toLowerCase();
        var rank = 0;
        var popular = List.of("西湖", "故宫", "迪士尼", "外滩", "环球度假区", "亚龙湾", "天涯海角", "鼓浪屿", "栈桥",
                "洱海", "丽江古城", "张家界", "九寨沟", "黄山", "泰山", "长白山", "兵马俑", "夫子庙", "宽窄巷子",
                "洪崖洞", "橘子洲", "青海湖", "颐和园", "圆明园", "东方明珠", "海昌", "长隆");
        if (popular.stream().anyMatch(text::contains)) rank += 8;
        if (containsAny(text, "风景名胜", "景区", "旅游景点", "国家级", "5a", "4a")) rank += 5;
        if (containsAny(text, "海滨浴场", "海滩", "沙滩", "海岛", "滨海公园")) rank += 5;
        if (containsAny(text, "公园", "博物馆", "古镇", "古城", "老街", "步行街", "夜市", "乐园", "动物园", "海洋馆")) rank += 3;
        if (photo(poi).isPresent()) rank += 1;
        return rank;
    }

    private boolean containsAny(String text, String... words) {
        if (text == null) return false;
        for (var word : words) {
            if (text.contains(word.toLowerCase())) return true;
        }
        return false;
    }

    private List<String> hotelKeywords(String keyword, String city) {
        var base = blankToDefault(keyword, "酒店").trim();
        var prefix = city == null || city.isBlank() ? "" : city.trim();
        var words = new ArrayList<String>();
        words.add(base);
        words.add(prefix + "酒店");
        words.add(prefix + "星级酒店");
        words.add(prefix + "高端酒店");
        words.add(prefix + "商务酒店");
        words.add(prefix + "度假酒店");
        words.add(prefix + "连锁酒店");
        words.add(prefix + "饭店");
        return words.stream().filter(x -> !x.isBlank()).distinct().toList();
    }

    private boolean isRegularHotel(Map<String, Object> poi) {
        var text = (text(poi, "name") + " " + text(poi, "type") + " " + text(poi, "address")).toLowerCase();
        if (text.isBlank()) return false;
        if (text.contains("公寓") || text.contains("民宿") || text.contains("客栈") || text.contains("青年旅舍")
                || text.contains("青旅") || text.contains("旅舍") || text.contains("旅社") || text.contains("旅馆")
                || text.contains("招待所") || text.contains("住宿") || text.contains("日租") || text.contains("短租")
                || text.contains("hostel") || text.contains("apartment") || text.contains("bnb")) {
            return false;
        }
        if (text.contains("宾馆") && !(text.contains("国宾馆") || text.contains("迎宾馆"))) {
            return false;
        }
        return text.contains("酒店") || text.contains("饭店") || text.contains("度假")
                || text.contains("hotel") || hotelRank(poi) > 0;
    }

    private int hotelRank(Map<String, Object> poi) {
        var text = (text(poi, "name") + " " + text(poi, "type")).toLowerCase();
        var rank = 0;
        if (text.contains("酒店") || text.contains("饭店") || text.contains("hotel")) rank += 3;
        if (text.contains("度假") || text.contains("resort")) rank += 3;
        if (text.contains("星级") || text.contains("豪华") || text.contains("国际")) rank += 2;
        var brands = List.of("希尔顿", "万豪", "洲际", "皇冠", "假日", "香格里拉", "凯悦", "君悦", "柏悦", "喜来登",
                "威斯汀", "丽思", "文华东方", "四季", "宝格丽", "W酒店", "万丽", "雅高", "美居", "诺富特",
                "亚朵", "全季", "桔子", "汉庭", "如家精选", "锦江都城", "丽枫");
        if (brands.stream().anyMatch(text::contains)) rank += 4;
        return rank;
    }

    private long stableId(Map<String, Object> poi) {
        return Math.abs((long) String.valueOf(poi.get("id")).hashCode());
    }

    private <T> T readCache(String key, TypeReference<T> type) {
        try {
            var value = redisTemplate.opsForValue().get(key);
            return value == null || value.isBlank() ? null : objectMapper.readValue(value, type);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void writeCache(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
        }
    }

    private String cacheKey(String type, String keyword, String city, int limit) {
        return "poi:%s:%s:%s:%d".formatted(type, normalize(keyword), normalize(city), limit);
    }

    private String normalize(String value) {
        return Optional.ofNullable(value).map(String::trim).filter(x -> !x.isBlank()).orElse("_");
    }
}
