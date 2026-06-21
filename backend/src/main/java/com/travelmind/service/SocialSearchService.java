package com.travelmind.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SocialSearchService {
    private final RestClient restClient = RestClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 TravelMindAI/1.0")
            .build();

    public List<SocialItem> search(String place, String city, String scene) {
        var keyword = buildKeyword(place, city, scene);
        var items = new ArrayList<SocialItem>();
        items.addAll(searchBing("site:xiaohongshu.com " + keyword, "小红书", "最新图文攻略", 3));
        items.addAll(searchBing("site:douyin.com " + keyword, "抖音", "热门视频/探店", 3));
        items.addAll(fallbackLinks(keyword));
        return distinct(items).stream().limit(10).toList();
    }

    private String buildKeyword(String place, String city, String scene) {
        var base = String.join(" ", List.of(nullToBlank(city), nullToBlank(place))).trim();
        if (base.isBlank()) base = "旅行";
        return switch (scene) {
            case "hotel" -> base + " 酒店 入住体验 周边美食";
            case "food" -> base + " 美食 探店";
            default -> base + " 打卡 攻略 美食";
        };
    }

    private List<SocialItem> searchBing(String query, String platform, String type, int limit) {
        try {
            var html = restClient.get()
                    .uri("https://www.bing.com/search?q=" + encode(query) + "&mkt=zh-CN")
                    .retrieve()
                    .body(String.class);
            if (html == null || html.isBlank()) return List.of();
            var matcher = Pattern.compile("<li class=\"b_algo\"[\\s\\S]*?<a href=\"(https?://[^\"]+)\"[^>]*>([\\s\\S]*?)</a>")
                    .matcher(html);
            var list = new ArrayList<SocialItem>();
            while (matcher.find() && list.size() < limit) {
                var url = cleanUrl(matcher.group(1));
                var title = cleanText(matcher.group(2));
                if (!title.isBlank() && isWanted(url, platform)) {
                    list.add(new SocialItem(platform, type, title, url, "来自实时搜索结果"));
                }
            }
            return list;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private List<SocialItem> fallbackLinks(String keyword) {
        var q = encode(keyword);
        return List.of(
                new SocialItem("小红书", "搜索入口", "查看小红书最新图文攻略", "https://www.xiaohongshu.com/search_result?keyword=" + q, "跳转后按热度或最新筛选"),
                new SocialItem("抖音", "搜索入口", "查看抖音热门视频/探店", "https://www.douyin.com/search/" + q, "跳转后查看点赞高的视频"),
                new SocialItem("全网", "搜索入口", "全网搜索旅行攻略", "https://www.bing.com/search?q=" + q, "用于补充更多实时信息")
        );
    }

    private List<SocialItem> distinct(List<SocialItem> items) {
        var seen = new HashSet<String>();
        return items.stream().filter(item -> seen.add(item.url())).toList();
    }

    private boolean isWanted(String url, String platform) {
        return switch (platform) {
            case "小红书" -> url.contains("xiaohongshu.com");
            case "抖音" -> url.contains("douyin.com");
            default -> true;
        };
    }

    private String cleanUrl(String url) {
        return url.replace("&amp;", "&");
    }

    private String cleanText(String html) {
        return html.replaceAll("<[^>]+>", "")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    public record SocialItem(String platform, String type, String title, String url, String summary) {
    }
}
