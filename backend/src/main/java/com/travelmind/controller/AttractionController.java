package com.travelmind.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travelmind.common.PageResp;
import com.travelmind.common.Result;
import com.travelmind.config.AppProperties;
import com.travelmind.mapper.AttractionMapper;
import com.travelmind.model.Attraction;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Month;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {
    private final AttractionMapper mapper;
    private final AppProperties properties;
    private final RestClient restClient = RestClient.create();

    @GetMapping
    public Result<PageResp<Attraction>> page(String keyword, String city, String tag,
                                            @RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "30") long size) {
        var hasKeyword = keyword != null && !keyword.isBlank();
        var query = lambdaQuery(Attraction.class)
                .and(hasKeyword, q -> q
                        .like(Attraction::getName, keyword)
                        .or().like(Attraction::getProvince, keyword)
                        .or().like(Attraction::getCity, keyword)
                        .or().like(Attraction::getDescription, keyword)
                        .or().like(Attraction::getTags, keyword))
                .and(city != null && !city.isBlank(), q -> q
                        .like(Attraction::getCity, city)
                        .or().like(Attraction::getProvince, city))
                .like(tag != null && !tag.isBlank(), Attraction::getTags, tag)
                .orderByDesc(Attraction::getScore);
        return Result.ok(PageResp.of(mapper.selectPage(Page.of(page, size), query)));
    }

    @GetMapping("/{id}")
    public Result<Attraction> detail(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @GetMapping("/online")
    public Result<List<Attraction>> online(String keyword, String city,
                                           @RequestParam(defaultValue = "20") int limit,
                                           @RequestParam(required = false) String key) {
        var amapKey = Optional.ofNullable(key)
                .filter(x -> !x.isBlank())
                .or(() -> Optional.ofNullable(properties.amap()).map(AppProperties.Amap::key).filter(x -> !x.isBlank()))
                .orElseThrow(() -> new IllegalArgumentException("缺少高德 AMAP_KEY"));
        var query = keyword == null || keyword.isBlank() ? "旅游景点" : keyword;
        var body = restClient.get()
                .uri(uri -> uri.scheme("https")
                        .host("restapi.amap.com")
                        .path("/v5/place/text")
                        .queryParam("key", amapKey)
                        .queryParam("keywords", query)
                        .queryParam("region", city == null ? "" : city)
                        .queryParam("city_limit", city != null && !city.isBlank())
                        .queryParam("show_fields", "photos,business")
                        .queryParam("page_size", Math.min(limit, 25))
                        .build())
                .retrieve()
                .body(Map.class);
        if (!"1".equals(String.valueOf(body.get("status")))) {
            throw new IllegalArgumentException(String.valueOf(body.getOrDefault("info", "高德联网搜索失败")));
        }
        var pois = (List<Map<String, Object>>) body.getOrDefault("pois", List.of());
        return Result.ok(pois.stream().map(this::toOnlineAttraction).toList());
    }

    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(mapper.selectList(lambdaQuery(Attraction.class)
                .orderByDesc(Attraction::getScore)
                .last("limit " + Math.min(limit, 50))));
    }

    @GetMapping("/season")
    public Result<?> season(@RequestParam(defaultValue = "6") int limit) {
        var season = currentSeason();
        return Result.ok(mapper.selectList(lambdaQuery(Attraction.class)
                .like(Attraction::getBestSeason, season)
                .notIn(Attraction::getProvince, foreignCountries())
                .last("ORDER BY CASE " +
                        "WHEN name IN ('九寨沟','呼伦贝尔大草原','青海湖','桂林漓江','长白山天池','张家界国家森林公园','青岛栈桥','鼓浪屿','大理洱海') THEN 0 " +
                        "ELSE 1 END, score DESC LIMIT " + Math.min(limit, 12))));
    }

    @GetMapping("/season/global")
    public Result<?> globalSeason(@RequestParam(defaultValue = "6") int limit) {
        var season = currentSeason();
        return Result.ok(mapper.selectList(lambdaQuery(Attraction.class)
                .like(Attraction::getBestSeason, season)
                .orderByDesc(Attraction::getScore)
                .last("limit " + Math.min(limit, 12))));
    }

    private String currentSeason() {
        Month month = LocalDate.now().getMonth();
        if (List.of(Month.MARCH, Month.APRIL, Month.MAY).contains(month)) return "春";
        if (List.of(Month.JUNE, Month.JULY, Month.AUGUST).contains(month)) return "夏";
        if (List.of(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER).contains(month)) return "秋";
        return "冬";
    }

    private Attraction toOnlineAttraction(Map<String, Object> poi) {
        var item = new Attraction();
        item.setId(Math.abs((long) String.valueOf(poi.get("id")).hashCode()));
        item.setName(text(poi, "name"));
        item.setProvince(text(poi, "pname"));
        item.setCity(Optional.of(text(poi, "cityname")).filter(x -> !x.isBlank()).orElse(text(poi, "adname")));
        item.setDescription(Optional.of(text(poi, "address")).filter(x -> !x.isBlank()).orElse(text(poi, "type")));
        item.setOpenTime("联网结果");
        item.setBestSeason("全年");
        item.setTags(text(poi, "type"));
        item.setPrice(BigDecimal.ZERO);
        var business = (Map<?, ?>) poi.get("business");
        var rating = Optional.ofNullable(business == null ? null : business.get("rating"))
                .map(String::valueOf)
                .filter(x -> !x.isBlank() && !"[]".equals(x))
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
        item.setScore(rating);
        var location = text(poi, "location").split(",");
        if (location.length == 2) {
            item.setLongitude(new BigDecimal(location[0]));
            item.setLatitude(new BigDecimal(location[1]));
        }
        item.setCoverImage(photo(poi).orElseGet(() -> staticMap(text(poi, "location"))));
        return item;
    }

    private String staticMap(String location) {
        var key = Optional.ofNullable(properties.amap()).map(AppProperties.Amap::key).orElse("");
        return location == null || location.isBlank() || key.isBlank()
                ? ""
                : "https://restapi.amap.com/v3/staticmap?location=" + location + "&zoom=13&size=600*320&markers=mid,,A:" + location + "&key=" + key;
    }

    private Optional<String> photo(Map<String, Object> poi) {
        var photos = (List<Map<String, Object>>) poi.getOrDefault("photos", List.of());
        return photos.stream().map(x -> String.valueOf(x.get("url"))).filter(x -> !x.isBlank() && !"null".equals(x)).findFirst();
    }

    private String text(Map<String, Object> map, String key) {
        var value = map.get(key);
        return value == null || "[]".equals(String.valueOf(value)) ? "" : String.valueOf(value);
    }

    private List<String> foreignCountries() {
        return List.of("日本", "韩国", "泰国", "新加坡", "马来西亚", "印度尼西亚", "越南", "柬埔寨", "菲律宾",
                "马尔代夫", "阿联酋", "土耳其", "法国", "英国", "意大利", "西班牙", "葡萄牙", "荷兰",
                "捷克", "奥地利", "瑞士", "德国", "希腊", "埃及", "南非", "美国", "加拿大", "墨西哥",
                "巴西", "阿根廷", "澳大利亚", "新西兰");
    }
}
