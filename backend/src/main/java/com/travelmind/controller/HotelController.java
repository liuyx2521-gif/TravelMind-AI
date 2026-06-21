package com.travelmind.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travelmind.common.PageResp;
import com.travelmind.common.Result;
import com.travelmind.mapper.HotelMapper;
import com.travelmind.model.Hotel;
import com.travelmind.service.AmapPoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
    private final HotelMapper mapper;
    private final AmapPoiService amapPoiService;

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
        return Result.ok(amapPoiService.searchHotels(keyword, city, limit, key));
    }
}
