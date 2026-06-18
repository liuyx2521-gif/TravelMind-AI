package com.travelmind.ai;

import com.travelmind.config.AppProperties;
import com.travelmind.mapper.AttractionMapper;
import com.travelmind.mapper.HotelMapper;
import com.travelmind.model.Attraction;
import com.travelmind.model.Hotel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@Service
@RequiredArgsConstructor
public class TravelSearchService {
    private final AttractionMapper attractionMapper;
    private final HotelMapper hotelMapper;
    private final AppProperties properties;
    private final RestClient restClient = RestClient.create();

    public String context(String input) {
        var city = guessCity(input);
        var sb = new StringBuilder();
        sb.append("以下是系统实时检索到的旅行参考数据，请优先基于这些数据回答；数据不足时再给建议，并明确说明需要出行前确认。\n");
        appendDestinationHints(sb, input);
        appendLocalAttractions(sb, input, city);
        appendLocalHotels(sb, input, city);
        appendOnlinePois(sb, input, city, "旅游景点", "联网景点");
        appendOnlinePois(sb, input, city, "酒店", "联网酒店");
        appendOnlinePois(sb, input, city, "美食", "联网美食");
        candidateCities(input).forEach(x -> {
            appendLocalAttractions(sb, x, x);
            appendLocalHotels(sb, x, x);
            appendOnlinePois(sb, input, x, x + " 景点", "候选目的地-" + x + "-景点");
            appendOnlinePois(sb, input, x, x + " 酒店", "候选目的地-" + x + "-酒店");
        });
        return sb.toString();
    }

    private void appendDestinationHints(StringBuilder sb, String input) {
        var cities = candidateCities(input);
        if (!cities.isEmpty()) {
            sb.append("\n【目的地候选】\n");
            sb.append("根据用户偏好，可优先比较：").append(String.join("、", cities)).append("。\n");
        }
    }

    private void appendLocalAttractions(StringBuilder sb, String input, String city) {
        var query = lambdaQuery(Attraction.class)
                .and(q -> q.like(Attraction::getName, input)
                        .or().like(Attraction::getCity, input)
                        .or().like(Attraction::getProvince, input)
                        .or().like(Attraction::getDescription, input)
                        .or().like(Attraction::getTags, input))
                .or(city != null && !city.isBlank(), q -> q.like(Attraction::getCity, city).or().like(Attraction::getProvince, city))
                .orderByDesc(Attraction::getScore)
                .last("limit 8");
        var list = attractionMapper.selectList(query);
        if (list.isEmpty()) return;
        sb.append("\n【本地景点库】\n");
        list.forEach(x -> sb.append("- %s｜%s%s｜评分%s｜门票约%s元｜季节%s｜%s\n".formatted(
                x.getName(), nullToEmpty(x.getProvince()), nullToEmpty(x.getCity()), x.getScore(), x.getPrice(),
                nullToEmpty(x.getBestSeason()), shortText(x.getDescription()))));
    }

    private void appendLocalHotels(StringBuilder sb, String input, String city) {
        var query = lambdaQuery(Hotel.class)
                .and(q -> q.like(Hotel::getName, input)
                        .or().like(Hotel::getCity, input)
                        .or().like(Hotel::getAddress, input))
                .or(city != null && !city.isBlank(), q -> q.like(Hotel::getCity, city))
                .orderByDesc(Hotel::getScore)
                .last("limit 8");
        var list = hotelMapper.selectList(query);
        if (list.isEmpty()) return;
        sb.append("\n【本地酒店库】\n");
        list.forEach(x -> sb.append("- %s｜%s｜评分%s｜参考价%s元｜%s\n".formatted(
                x.getName(), x.getCity(), x.getScore(), x.getPrice(), shortText(x.getAddress()))));
    }

    private void appendOnlinePois(StringBuilder sb, String input, String city, String keyword, String title) {
        var key = Optional.ofNullable(properties.amap()).map(AppProperties.Amap::key).filter(x -> !x.isBlank()).orElse("");
        if (key.isBlank()) return;
        try {
            var body = restClient.get()
                    .uri(uri -> uri.scheme("https")
                            .host("restapi.amap.com")
                            .path("/v5/place/text")
                            .queryParam("key", key)
                            .queryParam("keywords", city == null ? keyword : city + " " + keyword)
                            .queryParam("region", city == null ? "" : city)
                            .queryParam("city_limit", city != null && !city.isBlank())
                            .queryParam("show_fields", "photos,business")
                            .queryParam("page_size", 8)
                            .build())
                    .retrieve()
                    .body(Map.class);
            if (!"1".equals(String.valueOf(body.get("status")))) return;
            var pois = (List<Map<String, Object>>) body.getOrDefault("pois", List.of());
            if (pois.isEmpty()) return;
            sb.append("\n【").append(title).append("】\n");
            pois.stream().limit(8).forEach(poi -> sb.append("- %s｜%s｜%s｜评分%s｜%s\n".formatted(
                    text(poi, "name"), text(poi, "cityname"), text(poi, "address"), rating(poi), text(poi, "type"))));
        } catch (Exception ignored) {
        }
    }

    private String guessCity(String input) {
        return List.of("北京", "上海", "广州", "深圳", "杭州", "成都", "重庆", "西安", "南京", "苏州", "厦门", "青岛", "三亚",
                        "大理", "丽江", "桂林", "长沙", "武汉", "宁波", "舟山", "台州", "福州", "天津", "哈尔滨", "长春",
                        "沈阳", "大连", "昆明", "贵阳", "拉萨", "乌鲁木齐", "呼伦贝尔", "张家界", "香港", "澳门", "东京",
                        "大阪", "京都", "首尔", "曼谷", "新加坡", "巴黎", "伦敦", "罗马", "纽约", "洛杉矶")
                .stream()
                .filter(input::contains)
                .findFirst()
                .orElse(null);
    }

    private List<String> candidateCities(String input) {
        if (input.contains("海边") || input.contains("海岛") || input.contains("看海") || input.contains("沙滩")) {
            if (input.contains("杭州") || input.contains("上海") || input.contains("南京") || input.contains("苏州")) {
                return List.of("舟山", "宁波", "台州", "厦门");
            }
            return List.of("厦门", "青岛", "三亚", "舟山");
        }
        if (input.contains("雪") || input.contains("滑雪") || input.contains("冰雪")) {
            return List.of("哈尔滨", "长白山", "阿勒泰", "张家口");
        }
        if (input.contains("古镇") || input.contains("江南") || input.contains("水乡")) {
            return List.of("苏州", "嘉兴", "湖州", "黄山");
        }
        if (input.contains("亲子") || input.contains("孩子") || input.contains("迪士尼")) {
            return List.of("上海", "香港", "东京", "大阪");
        }
        if (input.contains("美食")) {
            return List.of("成都", "重庆", "长沙", "广州");
        }
        return List.of();
    }

    private String rating(Map<String, Object> poi) {
        var business = (Map<?, ?>) poi.get("business");
        return Optional.ofNullable(business == null ? null : business.get("rating"))
                .map(String::valueOf)
                .filter(x -> !x.isBlank() && !"[]".equals(x))
                .orElse("暂无");
    }

    private String text(Map<String, Object> map, String key) {
        var value = map.get(key);
        return value == null || "[]".equals(String.valueOf(value)) ? "" : String.valueOf(value);
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String shortText(String value) {
        if (value == null) return "";
        return value.length() <= 90 ? value : value.substring(0, 90) + "...";
    }
}
