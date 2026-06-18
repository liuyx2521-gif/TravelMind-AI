package com.travelmind.controller;

import com.travelmind.common.Result;
import com.travelmind.mapper.BrowseHistoryMapper;
import com.travelmind.model.BrowseHistory;
import com.travelmind.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {
    private final BrowseHistoryMapper mapper;

    @GetMapping
    public Result<?> list() {
        return Result.ok(mapper.selectList(lambdaQuery(BrowseHistory.class)
                .eq(BrowseHistory::getUserId, LoginUser.id())
                .orderByDesc(BrowseHistory::getCreateTime)
                .last("limit 100")));
    }

    @PostMapping
    public Result<BrowseHistory> add(@RequestBody BrowseHistory history) {
        history.setUserId(LoginUser.id());
        history.setCreateTime(LocalDateTime.now());
        mapper.delete(lambdaQuery(BrowseHistory.class)
                .eq(BrowseHistory::getUserId, history.getUserId())
                .eq(BrowseHistory::getTargetId, history.getTargetId())
                .eq(BrowseHistory::getTargetType, history.getTargetType()));
        mapper.insert(history);
        return Result.ok(history);
    }

    @DeleteMapping
    public Result<Void> clear() {
        mapper.delete(lambdaQuery(BrowseHistory.class).eq(BrowseHistory::getUserId, LoginUser.id()));
        return Result.ok();
    }
}
