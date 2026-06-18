package com.travelmind.controller;

import com.travelmind.ai.AiService;
import com.travelmind.common.Result;
import com.travelmind.mapper.TravelPlanMapper;
import com.travelmind.model.TravelPlan;
import com.travelmind.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
    private final TravelPlanMapper mapper;
    private final AiService aiService;

    @GetMapping
    public Result<?> list() {
        return Result.ok(mapper.selectList(lambdaQuery(TravelPlan.class)
                .eq(TravelPlan::getUserId, LoginUser.id())
                .orderByDesc(TravelPlan::getCreateTime)));
    }

    @GetMapping("/{id}")
    public Result<TravelPlan> detail(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping
    public Result<TravelPlan> create(@RequestBody TravelPlan plan) {
        plan.setUserId(LoginUser.id());
        mapper.insert(plan);
        return Result.ok(plan);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TravelPlan plan) {
        plan.setId(id);
        plan.setUserId(LoginUser.id());
        mapper.updateById(plan);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/generate")
    public Result<String> generate(@RequestBody AiService.PlanReq req) {
        return Result.ok(aiService.plan(req));
    }
}
