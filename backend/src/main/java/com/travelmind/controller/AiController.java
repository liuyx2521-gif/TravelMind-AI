package com.travelmind.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.travelmind.ai.AiService;
import com.travelmind.common.Result;
import com.travelmind.mapper.AiConversationMapper;
import com.travelmind.mapper.AiMessageMapper;
import com.travelmind.model.AiConversation;
import com.travelmind.model.AiMessage;
import com.travelmind.security.LoginUser;
import com.travelmind.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final StringRedisTemplate redisTemplate;
    private final RateLimitService rateLimitService;
    private static final int RECENT_LIMIT = 12;
    private static final int SUMMARY_AFTER_MESSAGES = 40;
    private static final int SUMMARY_REFRESH_STEP = 10;

    public record ChatResp(Long conversationId, String answer) {
    }

    @PostMapping("/chat")
    public Result<ChatResp> chat(@RequestBody AiService.ChatReq req) {
        rateLimitService.check("ai", 20, Duration.ofMinutes(1));
        var userId = LoginUser.optionalId().orElse(0L);
        var conversationId = conversationId(userId, req);
        var context = context(conversationId);
        saveMessage(conversationId, userId, "user", req.message());
        var answer = aiService.chat(new AiService.ChatReq(conversationId, context + "\nuser: " + req.message()));
        saveMessage(conversationId, userId, "assistant", answer);
        refreshSummaryIfNeeded(conversationId);
        cacheContext(conversationId, buildContext(conversationId));
        conversationMapper.update(null, Wrappers.<AiConversation>lambdaUpdate()
                .eq(AiConversation::getId, conversationId)
                .setSql("update_time = now()"));
        return Result.ok(new ChatResp(conversationId, answer));
    }

    @GetMapping("/config")
    public Result<AiService.AiConfig> config() {
        return Result.ok(aiService.config());
    }

    @PostMapping("/plan")
    public Result<String> plan(@RequestBody AiService.PlanReq req) {
        rateLimitService.check("ai", 12, Duration.ofMinutes(1));
        return Result.ok(aiService.plan(req));
    }

    @PostMapping("/budget")
    public Result<String> budget(@RequestBody AiService.BudgetReq req) {
        rateLimitService.check("ai", 12, Duration.ofMinutes(1));
        return Result.ok(aiService.budget(req));
    }

    @PostMapping("/hotels")
    public Result<String> hotels(@RequestBody AiService.RecommendReq req) {
        rateLimitService.check("ai", 12, Duration.ofMinutes(1));
        return Result.ok(aiService.hotels(req));
    }

    @PostMapping("/attractions")
    public Result<String> attractions(@RequestBody AiService.RecommendReq req) {
        rateLimitService.check("ai", 12, Duration.ofMinutes(1));
        return Result.ok(aiService.attractions(req));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> stream(@RequestBody AiService.ChatReq req) {
        rateLimitService.check("ai", 20, Duration.ofMinutes(1));
        var userId = LoginUser.optionalId().orElse(0L);
        var conversationId = conversationId(userId, req);
        var context = context(conversationId);
        saveMessage(conversationId, userId, "user", req.message());
        StreamingResponseBody body = output -> {
            var answer = new StringBuilder();
            writeSse(output, "meta", String.valueOf(conversationId));
            try {
                for (var text : aiService.stream(new AiService.ChatReq(conversationId, context + "\nuser: " + req.message())).toIterable()) {
                    answer.append(text);
                    writeSse(output, "delta", text);
                }
                finishStream(conversationId, userId, answer.toString());
                writeSse(output, "done", "done");
            } catch (RuntimeException e) {
                writeSse(output, "error", e.getMessage() == null ? "AI 回复失败" : e.getMessage());
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(body);
    }

    @GetMapping("/conversations")
    public Result<?> conversations() {
        var userId = LoginUser.optionalId().orElse(0L);
        return Result.ok(conversationMapper.selectList(Wrappers.<AiConversation>lambdaQuery()
                .eq(AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getUpdateTime)));
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<?> messages(@PathVariable Long id) {
        checkOwner(id, LoginUser.optionalId().orElse(0L));
        return Result.ok(messageMapper.selectList(Wrappers.<AiMessage>lambdaQuery()
                .eq(AiMessage::getConversationId, id)
                .orderByAsc(AiMessage::getCreateTime)));
    }

    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        deleteConversationById(id);
        return Result.ok();
    }

    @PostMapping("/conversations/{id}/delete")
    public Result<Void> deleteConversationByPost(@PathVariable Long id) {
        deleteConversationById(id);
        return Result.ok();
    }

    private void deleteConversationById(Long id) {
        checkOwner(id, LoginUser.optionalId().orElse(0L));
        messageMapper.delete(Wrappers.<AiMessage>lambdaQuery().eq(AiMessage::getConversationId, id));
        conversationMapper.deleteById(id);
        try {
            redisTemplate.delete(contextCacheKey(id));
        } catch (RuntimeException ignored) {
        }
    }

    private Long conversationId(Long userId, AiService.ChatReq req) {
        if (req.conversationId() != null) {
            checkOwner(req.conversationId(), userId);
            return req.conversationId();
        }
        var conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(req.message().length() > 20 ? req.message().substring(0, 20) : req.message());
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    private void saveMessage(Long conversationId, Long userId, String role, String content) {
        var msg = new AiMessage();
        msg.setConversationId(conversationId);
        msg.setUserId(userId);
        msg.setRole(role);
        msg.setContent(content);
        messageMapper.insert(msg);
    }

    private String context(Long conversationId) {
        var cacheKey = contextCacheKey(conversationId);
        try {
            var cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) return cached;
        } catch (RuntimeException ignored) {
        }
        var promptContext = buildContext(conversationId);
        cacheContext(conversationId, promptContext);
        return promptContext;
    }

    private String buildContext(Long conversationId) {
        var conversation = conversationMapper.selectById(conversationId);
        var summary = conversation == null ? "" : nullToBlank(conversation.getSummary());
        var recent = messageMapper.selectList(Wrappers.<AiMessage>lambdaQuery()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByDesc(AiMessage::getCreateTime)
                        .last("limit " + RECENT_LIMIT))
                .stream()
                .sorted(Comparator.comparing(AiMessage::getCreateTime))
                .map(this::line)
                .collect(Collectors.joining("\n"));
        return summary.isBlank()
                ? "最近消息：\n" + recent
                : "历史摘要：\n" + summary + "\n\n最近消息：\n" + recent;
    }

    private void cacheContext(Long conversationId, String context) {
        try {
            redisTemplate.opsForValue().set(contextCacheKey(conversationId), context, Duration.ofHours(24));
        } catch (RuntimeException ignored) {
        }
    }

    private void refreshSummaryIfNeeded(Long conversationId) {
        var total = messageMapper.selectCount(Wrappers.<AiMessage>lambdaQuery()
                .eq(AiMessage::getConversationId, conversationId)).intValue();
        if (total < SUMMARY_AFTER_MESSAGES) return;

        var conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) return;
        var summarizedCount = conversation.getSummaryMessageCount() == null ? 0 : conversation.getSummaryMessageCount();
        var targetCount = Math.max(0, total - RECENT_LIMIT);
        if (targetCount <= summarizedCount || targetCount - summarizedCount < SUMMARY_REFRESH_STEP) return;

        var history = messageMapper.selectList(Wrappers.<AiMessage>lambdaQuery()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByDesc(AiMessage::getCreateTime)
                        .last("limit " + Math.min(targetCount, 80)))
                .stream()
                .sorted(Comparator.comparing(AiMessage::getCreateTime))
                .map(this::line)
                .collect(Collectors.joining("\n"));
        try {
            var summary = aiService.summarizeConversation(conversation.getSummary(), history);
            conversationMapper.update(null, Wrappers.<AiConversation>lambdaUpdate()
                    .eq(AiConversation::getId, conversationId)
                    .set(AiConversation::getSummary, summary)
                    .set(AiConversation::getSummaryMessageCount, targetCount));
        } catch (RuntimeException ignored) {
        }
    }

    private void finishStream(Long conversationId, Long userId, String answer) {
        if (!answer.isBlank()) {
            saveMessage(conversationId, userId, "assistant", answer);
        }
        refreshSummaryIfNeeded(conversationId);
        cacheContext(conversationId, buildContext(conversationId));
        conversationMapper.update(null, Wrappers.<AiConversation>lambdaUpdate()
                .eq(AiConversation::getId, conversationId)
                .setSql("update_time = now()"));
    }

    private void writeSse(java.io.OutputStream output, String event, String data) throws java.io.IOException {
        var safeData = data == null ? "" : data.replace("\r", "");
        var builder = new StringBuilder("event: ").append(event).append("\n");
        for (var line : safeData.split("\n", -1)) {
            builder.append("data: ").append(line).append("\n");
        }
        builder.append("\n");
        output.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    private String line(AiMessage message) {
        return message.getRole() + ": " + nullToBlank(message.getContent());
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private String contextCacheKey(Long conversationId) {
        return "ai:conversation:" + conversationId;
    }

    private void checkOwner(Long conversationId, Long userId) {
        var conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            throw new IllegalArgumentException("会话不存在或无权访问");
        }
    }
}
