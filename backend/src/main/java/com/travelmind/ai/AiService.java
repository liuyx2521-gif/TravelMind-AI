package com.travelmind.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
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

    public record StructuredPlan(
            String title,
            String origin,
            String destination,
            Integer days,
            BigDecimal totalBudget,
            List<PlanDay> dayPlans,
            List<BudgetLine> budgetLines,
            List<HotelSuggestion> hotels,
            List<String> tips) {
    }

    public record PlanDay(Integer day, String theme, List<PlanNode> nodes) {
    }

    public record PlanNode(String type, String icon, String title, String time, String summary, String detail) {
    }

    public record BudgetLine(String category, BigDecimal amount, String note) {
    }

    public record HotelSuggestion(String name, String area, BigDecimal estimatedPrice, String reason) {
    }

    public AiConfig config() {
        return new AiConfig(provider, baseUrl, model, isOllama(), hasApiKey());
    }

    public String chat(ChatReq req) {
        return ask(prompt(req.message(), travelSearchService.context(req.message())));
    }

    public String summarizeConversation(String previousSummary, String history) {
        return ask("""
                请把下面的旅行规划对话压缩成“长期记忆摘要”，用于后续多轮对话。
                要求：
                - 只保留对后续规划有用的信息。
                - 包含出发地、目的地、天数、预算、偏好、排除项、已确认酒店/交通/行程、用户特别要求。
                - 不要写寒暄，不要扩展新信息。
                - 控制在 300 字以内。

                已有摘要：
                %s

                新增历史：
                %s
                """.formatted(
                previousSummary == null || previousSummary.isBlank() ? "暂无" : previousSummary,
                history == null || history.isBlank() ? "暂无" : history
        ));
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

    public StructuredPlan structuredPlan(PlanReq req) {
        var userInput = """
                你是 TravelMind AI 旅行规划师。请只返回 JSON，不要 Markdown，不要代码块。
                JSON 字段必须为：
                {
                  "title": "杭州到三亚3日游",
                  "origin": "出发地",
                  "destination": "目的地",
                  "days": 3,
                  "totalBudget": 3000,
                  "dayPlans": [
                    {
                      "day": 1,
                      "theme": "抵达与轻松适应",
                      "nodes": [
                        {"type":"transport","icon":"plane","title":"航班抵达","time":"上午","summary":"抵达目的地","detail":"完整建议、预约方式、避坑提醒"},
                        {"type":"attraction","icon":"beach","title":"景点","time":"下午","summary":"简短说明","detail":"完整建议、预约方式、避坑提醒"},
                        {"type":"food","icon":"bowl","title":"美食","time":"晚上","summary":"简短说明","detail":"完整建议、预约方式、避坑提醒"},
                        {"type":"hotel","icon":"hotel","title":"住宿","time":"夜间","summary":"简短说明","detail":"完整建议、预约方式、避坑提醒"}
                      ]
                    }
                  ],
                  "budgetLines": [
                    {"category":"交通","amount":1000,"note":"往返大交通估算"},
                    {"category":"酒店","amount":900,"note":"按入住晚数估算"}
                  ],
                  "hotels": [
                    {"name":"酒店名称","area":"所在区域","estimatedPrice":450,"reason":"推荐原因"}
                  ],
                  "tips": ["需要提前预约的事项"]
                }
                要求：
                - dayPlans 数量必须等于天数，每天 3-5 个节点。
                - 金额使用人民币数字，totalBudget 等于 budgetLines 合计。
                - 内容基于真实可查询地点，不编造不存在的景点或酒店。
                - 如目的地只是偏好，请先选择一个最合适目的地。

                出发地：%s
                目的地/偏好：%s
                预算：%s 元
                天数：%s 天
                偏好：%s
                """.formatted(req.origin(), req.destination(), req.budget(), req.days(), req.preference());
        var answer = ask(prompt(userInput, travelSearchService.context(userInput)));
        return parseStructuredPlan(answer, req);
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
            return typewriter(askOllama(prompt, baseUrl, model));
        }
        if (!hasApiKey()) {
            return typewriter(askLocalFallback(prompt, "DeepSeek API Key 未配置，已自动切换到本地免费模型。"));
        }
        var body = Map.of(
                "model", model,
                "stream", true,
                "messages", messages(prompt)
        );
        return client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(ServerSentEvent::data)
                .filter(Objects::nonNull)
                .filter(data -> !"[DONE]".equals(data))
                .map(this::streamText)
                .filter(text -> !text.isBlank())
                .onErrorResume(WebClientResponseException.Unauthorized.class,
                        e -> typewriter(askLocalFallback(prompt, "DeepSeek 认证失败，已自动切换到本地免费模型。")));
    }

    private StructuredPlan parseStructuredPlan(String answer, PlanReq req) {
        try {
            return objectMapper.readValue(extractJson(answer), StructuredPlan.class);
        } catch (Exception ignored) {
            return fallbackStructuredPlan(req);
        }
    }

    private String extractJson(String text) {
        if (text == null || text.isBlank()) return "{}";
        var cleaned = text.replace("```json", "").replace("```", "").trim();
        var start = cleaned.indexOf('{');
        var end = cleaned.lastIndexOf('}');
        return start >= 0 && end > start ? cleaned.substring(start, end + 1) : cleaned;
    }

    private StructuredPlan fallbackStructuredPlan(PlanReq req) {
        var budget = req.budget() == null ? BigDecimal.valueOf(3000) : req.budget();
        var days = req.days() == null || req.days() < 1 ? 3 : req.days();
        var destination = blankToDefault(req.destination(), "推荐目的地");
        var dayPlans = new ArrayList<PlanDay>();
        for (var day = 1; day <= days; day++) {
            dayPlans.add(new PlanDay(day, "第 " + day + " 天行程", List.of(
                    new PlanNode("transport", "subway", "城市交通", "上午", "前往当天主要区域", "根据酒店位置选择地铁、公交或打车，预留换乘和排队时间。"),
                    new PlanNode("attraction", "landmark", destination + "精选景点", "下午", "游览核心景点", "优先选择评分高、交通便利的真实景点，热门日期建议提前预约。"),
                    new PlanNode("food", "bowl", "当地美食", "傍晚", "安排特色餐食", "选择距离景点或酒店较近的餐厅，减少绕路和等待。"),
                    new PlanNode("hotel", "hotel", "返回酒店", "夜间", "休息调整", "确认次日交通时间，准备证件、充电设备和随身物品。")
            )));
        }
        var transport = budget.multiply(BigDecimal.valueOf(0.30));
        var hotel = budget.multiply(BigDecimal.valueOf(0.35));
        var food = budget.multiply(BigDecimal.valueOf(0.18));
        var attraction = budget.multiply(BigDecimal.valueOf(0.10));
        var other = budget.subtract(transport).subtract(hotel).subtract(food).subtract(attraction);
        return new StructuredPlan(
                blankToDefault(req.origin(), "出发地") + "到" + destination + days + "日游",
                blankToDefault(req.origin(), "出发地"),
                destination,
                days,
                budget,
                dayPlans,
                List.of(
                        new BudgetLine("交通", transport, "往返大交通与市内交通估算"),
                        new BudgetLine("酒店", hotel, "按入住晚数和舒适型住宿估算"),
                        new BudgetLine("餐饮", food, "按当地特色餐食和日常用餐估算"),
                        new BudgetLine("景点", attraction, "门票和体验项目估算"),
                        new BudgetLine("机动", other.max(BigDecimal.ZERO), "购物、保险和临时支出")
                ),
                List.of(new HotelSuggestion("目的地核心商圈酒店", "交通便利区域", hotel.divide(BigDecimal.valueOf(Math.max(days - 1, 1)), 0, java.math.RoundingMode.HALF_UP), "方便衔接景点、餐饮和公共交通")),
                List.of("热门景点和酒店价格会随日期波动，出发前建议再次确认。")
        );
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
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

    private Flux<String> typewriter(String text) {
        var chars = new ArrayList<String>();
        text.codePoints().forEach(cp -> chars.add(new String(Character.toChars(cp))));
        return Flux.fromIterable(chars).delayElements(Duration.ofMillis(18));
    }

    private String streamText(String data) {
        try {
            return objectMapper.readTree(data).at("/choices/0/delta/content").asText();
        } catch (Exception ignored) {
            return "";
        }
    }
}
