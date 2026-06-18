package com.travelmind.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Amap amap) {
    public record Amap(String key) {}
}
