package com.travelmind.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.travelmind.ai.AiService;
import com.travelmind.common.Result;
import com.travelmind.mapper.AiConversationMapper;
import com.travelmind.mapper.AiMessageMapper;
import com.travelmind.model.AiConversation;
import com.travelmind.model.AiMessage;
import com.travelmind.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    public record ChatResp(Long conversationId, String answer) {
    }

    @PostMapping("/chat")
    public Result<ChatResp> chat(@RequestBody AiService.ChatReq req) {
        var userId = LoginUser.optionalId().orElse(0L);
        var conversationId = conversationId(userId, req);
        var context = messageMapper.selectList(Wrappers.<AiMessage>lambdaQuery()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByDesc(AiMessage::getCreateTime)
                        .last("limit 8"))
                .stream()
                .sorted(Comparator.comparing(AiMessage::getCreateTime))
                .map(x -> x.getRole() + ": " + x.getContent())
                .collect(Collectors.joining("\n"));
        saveMessage(conversationId, userId, "user", req.message());
        var answer = aiService.chat(new AiService.ChatReq(conversationId, context + "\nuser: " + req.message()));
        saveMessage(conversationId, userId, "assistant", answer);
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
        return Result.ok(aiService.plan(req));
    }

    @PostMapping("/budget")
    public Result<String> budget(@RequestBody AiService.BudgetReq req) {
        return Result.ok(aiService.budget(req));
    }

    @PostMapping("/hotels")
    public Result<String> hotels(@RequestBody AiService.RecommendReq req) {
        return Result.ok(aiService.hotels(req));
    }

    @PostMapping("/attractions")
    public Result<String> attractions(@RequestBody AiService.RecommendReq req) {
        return Result.ok(aiService.attractions(req));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody AiService.ChatReq req) {
        return aiService.stream(req);
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
        return Result.ok(messageMapper.selectList(Wrappers.<AiMessage>lambdaQuery()
                .eq(AiMessage::getConversationId, id)
                .orderByAsc(AiMessage::getCreateTime)));
    }

    private Long conversationId(Long userId, AiService.ChatReq req) {
        if (req.conversationId() != null) {
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
}
