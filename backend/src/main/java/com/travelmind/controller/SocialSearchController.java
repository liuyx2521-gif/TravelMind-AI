package com.travelmind.controller;

import com.travelmind.common.Result;
import com.travelmind.service.SocialSearchService;
import com.travelmind.service.SocialSearchService.SocialItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialSearchController {
    private final SocialSearchService socialSearchService;

    @GetMapping("/search")
    public Result<List<SocialItem>> search(String place, String city,
                                           @RequestParam(defaultValue = "attraction") String scene) {
        return Result.ok(socialSearchService.search(place, city, scene));
    }
}
