package com.travelmind.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class AiService {
    private final WebClient client;
    private final ObjectMapper objectMapper;
    private final String provider;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final String localBaseUrl;
    private final String localModel;
    private final TravelSearchService travelSearchService;

    public AiService(
            WebClient.Builder builder,
            ObjectMapper objectMapper,
            TravelSearchService travelSearchService,
            @Value("${app.ai.provider:deepseek}") String provider,
            @Value("${app.ai.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:deepseek-chat}") String model,
            @Value("${app.ai.local-base-url:http://localhost:11434}") String localBaseUrl,
            @Value("${app.ai.local-model:qwen3.5:0.8b}") String localModel) {
        this.client = builder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
        this.travelSearchService = travelSearchService;
        this.provider = provider;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.localBaseUrl = localBaseUrl;
        this.localModel = localModel;
    }

    public record AiConfig(String provider, String baseUrl, String model, boolean freeByDefault, boolean apiKeyConfigured) {
    }

    public record ChatReq(Long conversationId, String message) {
    }

    public record PlanReq(String origin, String destination, BigDecimal budget, Integer days, String preference) {
    }

    public record BudgetReq(BigDecimal hotel, BigDecimal transport, BigDecimal food, BigDecimal ticket, BigDecimal other) {
    }

    public record RecommendReq(String city, BigDecimal budget, Integer days, String preference, String peopleType) {
    }

    public AiConfig config() {
        return new AiConfig(provider, baseUrl, model, isOllama(), hasApiKey());
    }

    public String chat(ChatReq req) {
        return ask(prompt(req.message(), travelSearchService.context(req.message())));
    }

    public String plan(PlanReq req) {
        var userInput = """
                你是 TravelMind AI 旅行规划师。
                请根据以下信息生成旅行方案，包含推荐原因、最佳时间、交通、预算、酒店、美食、打卡点、每日行程。
                出发地：%s
                目的地/偏好：%s
                预算：%s 元
                天数：%s 天
                偏好：%s
                输出 Markdown，内容简洁可执行。
                """.formatted(req.origin(), req.destination(), req.budget(), req.days(), req.preference());
        return ask(prompt(userInput, travelSearchService.context(userInput)));
    }

    public String budget(BudgetReq req) {
        var total = Stream.of(req.hotel(), req.transport(), req.food(), req.ticket(), req.other())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ask(prompt("""
                请做一份旅行预算分析。
                酒店：%s
                交通：%s
                餐饮：%s
                门票：%s
                其他：%s
                总预算：%s
                请指出是否合理、哪里可以压缩、如何分配更舒服。
                """.formatted(req.hotel(), req.transport(), req.food(), req.ticket(), req.other(), total), ""));
    }

    public String hotels(RecommendReq req) {
        var userInput = """
                请根据用户条件推荐酒店类型和入住区域。
                城市：%s
                预算：%s
                天数：%s
                偏好：%s
                人群：%s
                输出酒店区域、价格区间、选择理由、避坑建议。
                """.formatted(req.city(), req.budget(), req.days(), req.preference(), req.peopleType());
        return ask(prompt(userInput, travelSearchService.context(userInput)));
    }

    public String attractions(RecommendReq req) {
        var userInput = """
                请根据偏好推荐景点。
                城市：%s
                预算：%s
                天数：%s
                偏好：%s
                人群：%s
                输出景点、推荐理由、最佳游玩时间、打卡点和预计费用。
                """.formatted(req.city(), req.budget(), req.days(), req.preference(), req.peopleType());
        return ask(prompt(userInput, travelSearchService.context(userInput)));
    }

    public Flux<String> stream(ChatReq req) {
        var prompt = prompt(req.message(), travelSearchService.context(req.message()));
        if (isOllama()) {
            return Flux.just(askOllama(prompt, baseUrl, model));
        }
        if (!hasApiKey()) {
            return Flux.just(askLocalFallback(prompt, "DeepSeek API Key 未配置，已自动切换到本地免费模型。"));
        }
        var body = Map.of(
                "model", model,
                "stream", true,
                "messages", messages(prompt)
        );
        return client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(WebClientResponseException.Unauthorized.class,
                        e -> Flux.just(askLocalFallback(prompt, "DeepSeek 认证失败，已自动切换到本地免费模型。")));
    }

    private String ask(String prompt) {
        if (isOllama()) {
            return askOllama(prompt, baseUrl, model);
        }
        if (!hasApiKey()) {
            return askLocalFallback(prompt, "DeepSeek API Key 未配置，已自动切换到本地免费模型。");
        }
        var body = Map.of(
                "model", model,
                "stream", false,
                "messages", messages(prompt)
        );
        try {
            return client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(x -> x.at("/choices/0/message/content").asText())
                    .block();
        } catch (WebClientResponseException.Unauthorized e) {
            return askLocalFallback(prompt, "DeepSeek 认证失败，已自动切换到本地免费模型。");
        }
    }

    private String askLocalFallback(String prompt, String reason) {
        return reason + "\n\n" + askOllama(prompt, localBaseUrl, localModel);
    }

    private String askOllama(String prompt, String url, String modelName) {
        try {
            var body = objectMapper.writeValueAsString(Map.of(
                    "model", modelName,
                    "stream", false,
                    "think", false,
                    "messages", messages(prompt),
                    "options", Map.of("temperature", 0.7, "num_predict", 500)
            ));
            var request = HttpRequest.newBuilder(URI.create(url + "/api/chat"))
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(response.body());
            }
            return objectMapper.readTree(response.body()).at("/message/content").asText();
        } catch (Exception e) {
            throw new IllegalStateException("本地 AI 调用失败：" + e.getMessage(), e);
        }
    }

    private boolean isOllama() {
        return "ollama".equalsIgnoreCase(provider);
    }

    private boolean hasApiKey() {
        return !apiKey.isBlank() && !"local".equalsIgnoreCase(apiKey);
    }

    private String prompt(String userInput, String travelContext) {
        return """
                你是 TravelMind AI，专注中文旅行问答、目的地推荐、行程规划和预算分析。
                下面可能包含历史对话上下文，请基于上下文回答用户最后一个问题。
                回答要求：
                - 直接回答，不要重复系统说明。
                - 内容用清晰的中文文本和必要的小标题。
                - 涉及费用请用人民币粗略估算，并说明不确定因素。
                - 优先使用“旅行检索数据”中的真实景点、酒店、美食和 POI 信息。
                - 不要编造不存在的景点、酒店、评分、价格或开放时间。
                - 如果信息不足，先给可执行建议，再补一句需要用户确认什么。

                旅行检索数据：
                %s

                对话内容如下：
                %s
                """.formatted(travelContext == null || travelContext.isBlank() ? "暂无额外检索数据。" : travelContext, userInput);
    }

    private List<Map<String, String>> messages(String prompt) {
        return List.of(
                Map.of("role", "system", "content", "你是专业、可靠、简洁的中文旅行规划师。回答自然、有上下文记忆，优先给可执行建议。"),
                Map.of("role", "user", "content", prompt)
        );
    }
}
