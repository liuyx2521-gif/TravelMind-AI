package com.travelmind.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmind.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final RestClient restClient = RestClient.create();

    public record WeatherInfo(
            String city,
            String province,
            String weather,
            String temperature,
            String windDirection,
            String windPower,
            String humidity,
            String reportTime,
            List<Forecast> forecasts
    ) {
        public String summary() {
            var forecastText = forecasts == null || forecasts.isEmpty()
                    ? ""
                    : " 未来天气：" + forecasts.stream()
                    .limit(3)
                    .map(x -> "%s %s，%s-%s℃".formatted(x.date(), x.dayWeather(), x.nightTemp(), x.dayTemp()))
                    .reduce((a, b) -> a + "；" + b)
                    .orElse("");
            return "%s%s：%s，%s℃，湿度%s%%，%s风%s级，更新时间%s。%s"
                    .formatted(nullToBlank(province), city, weather, temperature, humidity, windDirection, windPower, reportTime, forecastText);
        }

        private static String nullToBlank(String value) {
            return value == null ? "" : value;
        }
    }

    public record Forecast(String date, String week, String dayWeather, String nightWeather, String dayTemp, String nightTemp) {
    }

    public WeatherInfo current(String city) {
        var query = Optional.ofNullable(city).map(String::trim).filter(x -> !x.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("城市不能为空"));
        var key = configuredKey();
        var cacheKey = "weather:" + query;
        var cached = readCache(cacheKey);
        if (cached != null) return cached;

        var adcode = adcode(query, key);
        var weather = queryWeather(adcode, key);
        writeCache(cacheKey, weather);
        return weather;
    }

    public String context(String city) {
        try {
            return current(city).summary();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private WeatherInfo queryWeather(String adcode, String key) {
        var liveBody = restClient.get()
                .uri(uri -> uri.scheme("https")
                        .host("restapi.amap.com")
                        .path("/v3/weather/weatherInfo")
                        .queryParam("key", key)
                        .queryParam("city", adcode)
                        .queryParam("extensions", "base")
                        .build())
                .retrieve()
                .body(JsonNode.class);
        if (liveBody == null || !"1".equals(liveBody.path("status").asText())) {
            throw new IllegalArgumentException(liveBody == null ? "天气查询失败" : liveBody.path("info").asText("天气查询失败"));
        }
        var live = liveBody.path("lives").path(0);
        if (live.isMissingNode()) throw new IllegalArgumentException("暂未查询到该城市天气");
        return new WeatherInfo(
                live.path("city").asText(),
                live.path("province").asText(),
                live.path("weather").asText(),
                live.path("temperature").asText(),
                live.path("winddirection").asText(),
                live.path("windpower").asText(),
                live.path("humidity").asText(),
                live.path("reporttime").asText(),
                forecasts(adcode, key)
        );
    }

    private List<Forecast> forecasts(String adcode, String key) {
        try {
            var body = restClient.get()
                    .uri(uri -> uri.scheme("https")
                            .host("restapi.amap.com")
                            .path("/v3/weather/weatherInfo")
                            .queryParam("key", key)
                            .queryParam("city", adcode)
                            .queryParam("extensions", "all")
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null || !"1".equals(body.path("status").asText())) return List.of();
            var casts = body.path("forecasts").path(0).path("casts");
            var list = new ArrayList<Forecast>();
            casts.forEach(x -> list.add(new Forecast(
                    x.path("date").asText(),
                    x.path("week").asText(),
                    x.path("dayweather").asText(),
                    x.path("nightweather").asText(),
                    x.path("daytemp").asText(),
                    x.path("nighttemp").asText()
            )));
            return list;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private String adcode(String city, String key) {
        var body = restClient.get()
                .uri(uri -> uri.scheme("https")
                        .host("restapi.amap.com")
                        .path("/v3/geocode/geo")
                        .queryParam("key", key)
                        .queryParam("address", city)
                        .build())
                .retrieve()
                .body(JsonNode.class);
        if (body == null || !"1".equals(body.path("status").asText())) {
            throw new IllegalArgumentException(body == null ? "城市解析失败" : body.path("info").asText("城市解析失败"));
        }
        var adcode = body.path("geocodes").path(0).path("adcode").asText();
        if (adcode.isBlank()) throw new IllegalArgumentException("暂未识别该城市");
        return adcode;
    }

    private WeatherInfo readCache(String key) {
        try {
            var value = redisTemplate.opsForValue().get(key);
            return value == null ? null : objectMapper.readValue(value, WeatherInfo.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void writeCache(String key, WeatherInfo weather) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(weather), Duration.ofMinutes(30));
        } catch (Exception ignored) {
        }
    }

    private String configuredKey() {
        return Optional.ofNullable(properties.amap())
                .map(AppProperties.Amap::key)
                .filter(x -> !x.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("缺少高德 AMAP_KEY"));
    }
}
