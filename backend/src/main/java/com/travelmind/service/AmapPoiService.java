package com.travelmind.service;

import com.travelmind.config.AppProperties;
import com.travelmind.model.Attraction;
import com.travelmind.model.Hotel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
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
    private final RestClient restClient = RestClient.create();

    public record PoiSummary(String name, String city, String address, String rating, String type) {
        public String line() {
            return "%s｜%s｜%s｜评分%s｜%s".formatted(name, city, address, rating, type);
        }
    }

    public List<Attraction> searchAttractions(String keyword, String city, int limit, String requestKey) {
        var pois = searchRaw(blankToDefault(keyword, "旅游景点"), city, null, limit, resolveKey(requestKey));
        return pois.stream().map(this::toAttraction).toList();
    }

    public List<Hotel> searchHotels(String keyword, String city, int limit, String requestKey) {
        var key = resolveKey(requestKey);
        var size = Math.min(Math.max(limit, 1), 40);
        var keywords = hotelKeywords(keyword, city);
        var merged = new LinkedHashMap<Long, Map<String, Object>>();
        for (var word : keywords) {
            searchRaw(word, city, "100000", Math.min(25, Math.max(size, 12)), key).forEach(poi -> merged.putIfAbsent(stableId(poi), poi));
            if (merged.size() >= size * 3) break;
        }
        return merged.values().stream()
                .filter(this::isRegularHotel)
                .sorted(Comparator.comparingInt(this::hotelRank).reversed()
                        .thenComparing((Map<String, Object> poi) -> decimal(businessValue(poi, "rating")).orElse(BigDecimal.ZERO), Comparator.reverseOrder()))
                .limit(size)
                .map(this::toHotel)
                .toList();
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
}
