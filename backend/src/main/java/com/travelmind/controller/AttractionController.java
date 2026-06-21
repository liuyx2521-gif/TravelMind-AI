package com.travelmind.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travelmind.common.PageResp;
import com.travelmind.common.Result;
import com.travelmind.mapper.AttractionMapper;
import com.travelmind.model.Attraction;
import com.travelmind.service.AmapPoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.time.LocalDate;
import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {
    private final AttractionMapper mapper;
    private final AmapPoiService amapPoiService;

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
        return Result.ok(amapPoiService.searchAttractions(keyword, city, limit, key));
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

    private List<String> foreignCountries() {
        return List.of("日本", "韩国", "泰国", "新加坡", "马来西亚", "印度尼西亚", "越南", "柬埔寨", "菲律宾",
                "马尔代夫", "阿联酋", "土耳其", "法国", "英国", "意大利", "西班牙", "葡萄牙", "荷兰",
                "捷克", "奥地利", "瑞士", "德国", "希腊", "埃及", "南非", "美国", "加拿大", "墨西哥",
                "巴西", "阿根廷", "澳大利亚", "新西兰");
    }
}
