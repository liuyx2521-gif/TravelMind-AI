package com.travelmind.controller;

import com.travelmind.common.Result;
import com.travelmind.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping
    public Result<WeatherService.WeatherInfo> current(@RequestParam String city) {
        return Result.ok(weatherService.current(city));
    }
}
