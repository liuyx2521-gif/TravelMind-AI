package com.travelmind.controller;

import com.travelmind.common.Result;
import com.travelmind.mapper.CheckPointMapper;
import com.travelmind.model.CheckPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/check-points")
@RequiredArgsConstructor
public class CheckPointController {
    private final CheckPointMapper mapper;

    @GetMapping
    public Result<?> list(Long attractionId) {
        return Result.ok(mapper.selectList(lambdaQuery(CheckPoint.class)
                .eq(attractionId != null, CheckPoint::getAttractionId, attractionId)));
    }
}
