package com.travelmind.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travelmind.common.PageResp;
import com.travelmind.common.Result;
import com.travelmind.config.AppProperties;
import com.travelmind.mapper.HotelMapper;
import com.travelmind.model.Hotel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
    private final HotelMapper mapper;
    private final AppProperties properties;
    private final RestClient restClient = RestClient.create();

    @GetMapping
    public Result<PageResp<Hotel>> page(String city, String sort,
                                       @RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "30") long size) {
        var query = lambdaQuery(Hotel.class)
                .eq(city != null && !city.isBlank(), Hotel::getCity, city)
                .orderByAsc("price".equals(sort), Hotel::getPrice)
                .orderByDesc(!"price".equals(sort), Hotel::getScore);
        return Result.ok(PageResp.of(mapper.selectPage(Page.of(page, size), query)));
    }

    @GetMapping("/{id}")
    public Result<Hotel> detail(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @GetMapping("/nearby")
    public Result<?> nearby(String city, @RequestParam(defaultValue = "6") int limit) {
        return Result.ok(mapper.selectList(lambdaQuery(Hotel.class)
                .eq(city != null && !city.isBlank(), Hotel::getCity, city)
                .orderByDesc(Hotel::getScore)
                .last("limit " + Math.min(limit, 12))));
    }

    @GetMapping("/online")
    public Result<List<Hotel>> online(String keyword, String city,
                                      @RequestParam(defaultValue = "24") int limit,
                                      @RequestParam(required = false) String key) {
        var amapKey = Optional.ofNullable(key)
                .filter(x -> !x.isBlank())
                .or(() -> Optional.ofNullable(properties.amap()).map(AppProperties.Amap::key).filter(x -> !x.isBlank()))
                .orElseThrow(() -> new IllegalArgumentException("缺少高德 AMAP_KEY"));
        var query = keyword == null || keyword.isBlank() ? "酒店" : keyword;
        var body = restClient.get()
                .uri(uri -> uri.scheme("https")
                        .host("restapi.amap.com")
                        .path("/v5/place/text")
                        .queryParam("key", amapKey)
                        .queryParam("keywords", query)
                        .queryParam("types", "100000")
                        .queryParam("region", city == null ? "" : city)
                        .queryParam("city_limit", city != null && !city.isBlank())
                        .queryParam("show_fields", "photos,business")
                        .queryParam("page_size", Math.min(limit, 25))
                        .build())
                .retrieve()
                .body(Map.class);
        if (!"1".equals(String.valueOf(body.get("status")))) {
            throw new IllegalArgumentException(String.valueOf(body.getOrDefault("info", "高德酒店搜索失败")));
        }
        var pois = (List<Map<String, Object>>) body.getOrDefault("pois", List.of());
        return Result.ok(pois.stream().map(this::toOnlineHotel).toList());
    }

    private Hotel toOnlineHotel(Map<String, Object> poi) {
        var item = new Hotel();
        item.setId(Math.abs((long) String.valueOf(poi.get("id")).hashCode()));
        item.setName(text(poi, "name"));
        item.setCity(Optional.of(text(poi, "cityname")).filter(x -> !x.isBlank()).orElse(text(poi, "adname")));
        item.setAddress(text(poi, "address"));
        item.setPrice(price(poi));
        item.setScore(rating(poi));
        var location = text(poi, "location").split(",");
        if (location.length == 2) {
            item.setLongitude(new BigDecimal(location[0]));
            item.setLatitude(new BigDecimal(location[1]));
        }
        item.setCover(photo(poi).orElseGet(() -> staticMap(text(poi, "location"))));
        return item;
    }

    private String staticMap(String location) {
        var key = Optional.ofNullable(properties.amap()).map(AppProperties.Amap::key).orElse("");
        return location == null || location.isBlank() || key.isBlank()
                ? ""
                : "https://restapi.amap.com/v3/staticmap?location=" + location + "&zoom=13&size=600*320&markers=mid,,A:" + location + "&key=" + key;
    }

    private BigDecimal rating(Map<String, Object> poi) {
        var business = (Map<?, ?>) poi.get("business");
        return Optional.ofNullable(business == null ? null : business.get("rating"))
                .map(String::valueOf)
                .filter(x -> !x.isBlank() && !"[]".equals(x))
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal price(Map<String, Object> poi) {
        var business = (Map<?, ?>) poi.get("business");
        return Optional.ofNullable(business == null ? null : business.get("cost"))
                .map(String::valueOf)
                .filter(x -> !x.isBlank() && !"[]".equals(x))
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
    }

    private Optional<String> photo(Map<String, Object> poi) {
        var photos = (List<Map<String, Object>>) poi.getOrDefault("photos", List.of());
        return photos.stream().map(x -> String.valueOf(x.get("url"))).filter(x -> !x.isBlank() && !"null".equals(x)).findFirst();
    }

    private String text(Map<String, Object> map, String key) {
        var value = map.get(key);
        return value == null || "[]".equals(String.valueOf(value)) ? "" : String.valueOf(value);
    }
}
