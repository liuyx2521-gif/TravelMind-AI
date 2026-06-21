package com.travelmind.ai;

import com.travelmind.mapper.AttractionMapper;
import com.travelmind.mapper.HotelMapper;
import com.travelmind.model.Attraction;
import com.travelmind.model.Hotel;
import com.travelmind.service.AmapPoiService;
import com.travelmind.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@Service
@RequiredArgsConstructor
public class TravelSearchService {
    private final AttractionMapper attractionMapper;
    private final HotelMapper hotelMapper;
    private final AmapPoiService amapPoiService;
    private final WeatherService weatherService;

    public String context(String input) {
        var safeInput = input == null ? "" : input;
        var city = destinationCity(safeInput);
        var sb = new StringBuilder();
        sb.append("以下是系统实时检索到的旅行参考数据，请优先基于这些数据回答；数据不足时再给建议，并明确说明需要出行前确认。\n");
        appendWeather(sb, city);
        appendDestinationHints(sb, safeInput);
        appendLocalAttractions(sb, safeInput, city);
        appendLocalHotels(sb, safeInput, city);
        appendOnlinePois(sb, city, "旅游景点", "联网景点");
        appendOnlinePois(sb, city, "酒店", "联网酒店");
        appendOnlinePois(sb, city, "美食", "联网美食");
        candidateCities(safeInput).forEach(x -> {
            appendLocalAttractions(sb, x, x);
            appendLocalHotels(sb, x, x);
            appendOnlinePois(sb, x, x + " 景点", "候选目的地-" + x + "-景点");
            appendOnlinePois(sb, x, x + " 酒店", "候选目的地-" + x + "-酒店");
        });
        return sb.toString();
    }

    private void appendWeather(StringBuilder sb, String city) {
        if (city == null || city.isBlank()) return;
        var weather = weatherService.context(city);
        if (!weather.isBlank()) {
            sb.append("\n【实时天气】\n").append(weather).append("\n");
        }
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

    private void appendOnlinePois(StringBuilder sb, String city, String keyword, String title) {
        var summaries = amapPoiService.searchSummaries(city == null ? keyword : city + " " + keyword, city, 8);
        if (summaries.isEmpty()) return;
        sb.append("\n【").append(title).append("】\n");
        summaries.forEach(poi -> sb.append("- ").append(poi.line()).append("\n"));
    }

    private String guessCity(String input) {
        return knownCities()
                .stream()
                .filter(input::contains)
                .findFirst()
                .orElse(null);
    }

    private String destinationCity(String input) {
        var patterns = List.of(
                "从([^，。,.\\s]{2,8})(?:出发)?(?:去|到|前往)([^，。,.\\s]{2,8})",
                "([^，。,.\\s]{2,8})(?:出发)?(?:去|到|前往)([^，。,.\\s]{2,8})",
                "(?:目的地|推荐城市|推荐目的地)[：:\\s]*([^，。,.\\s]{2,8})",
                "去([^，。,.\\s]{2,8})(?:玩|旅游|旅行|打卡|吃|看|$)"
        );
        for (var pattern : patterns) {
            var matcher = Pattern.compile(pattern).matcher(input);
            if (!matcher.find()) continue;
            var value = matcher.group(matcher.groupCount());
            var city = knownCities().stream().filter(value::contains).findFirst().orElse(value);
            if (knownCities().contains(city)) return city;
        }
        return guessCity(input);
    }

    private List<String> knownCities() {
        return List.of("北京", "上海", "广州", "深圳", "杭州", "成都", "重庆", "西安", "南京", "苏州", "厦门", "青岛", "三亚",
                "大理", "丽江", "桂林", "长沙", "武汉", "宁波", "舟山", "台州", "福州", "天津", "哈尔滨", "长春",
                "沈阳", "大连", "昆明", "贵阳", "拉萨", "乌鲁木齐", "呼伦贝尔", "张家界", "香港", "澳门", "东京",
                "大阪", "京都", "首尔", "曼谷", "新加坡", "巴黎", "伦敦", "罗马", "纽约", "洛杉矶");
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

    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String shortText(String value) {
        if (value == null) return "";
        return value.length() <= 90 ? value : value.substring(0, 90) + "...";
    }
}
