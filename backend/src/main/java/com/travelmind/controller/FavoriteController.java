package com.travelmind.controller;

import com.travelmind.common.Result;
import com.travelmind.mapper.FavoriteMapper;
import com.travelmind.model.Favorite;
import com.travelmind.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteMapper mapper;

    @GetMapping
    public Result<?> list(String type) {
        return Result.ok(mapper.selectList(lambdaQuery(Favorite.class)
                .eq(Favorite::getUserId, LoginUser.id())
                .eq(type != null && !type.isBlank(), Favorite::getTargetType, type)
                .orderByDesc(Favorite::getCreateTime)));
    }

    @PostMapping
    public Result<Favorite> add(@RequestBody Favorite favorite) {
        favorite.setUserId(LoginUser.id());
        favorite.setCreateTime(LocalDateTime.now());
        var old = mapper.selectOne(lambdaQuery(Favorite.class)
                .eq(Favorite::getUserId, favorite.getUserId())
                .eq(Favorite::getTargetId, favorite.getTargetId())
                .eq(Favorite::getTargetType, favorite.getTargetType())
                .last("limit 1"));
        if (old != null) {
            favorite.setId(old.getId());
            mapper.updateById(favorite);
            return Result.ok(favorite);
        }
        mapper.insert(favorite);
        return Result.ok(favorite);
    }

    @DeleteMapping
    public Result<Void> delete(Long targetId, String targetType) {
        mapper.delete(lambdaQuery(Favorite.class)
                .eq(Favorite::getUserId, LoginUser.id())
                .eq(Favorite::getTargetId, targetId)
                .eq(Favorite::getTargetType, targetType));
        return Result.ok();
    }

    @DeleteMapping("/all")
    public Result<Void> clear() {
        mapper.delete(lambdaQuery(Favorite.class).eq(Favorite::getUserId, LoginUser.id()));
        return Result.ok();
    }
}
