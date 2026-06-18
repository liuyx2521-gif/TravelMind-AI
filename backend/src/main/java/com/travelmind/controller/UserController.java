package com.travelmind.controller;

import com.travelmind.common.Result;
import com.travelmind.mapper.UserMapper;
import com.travelmind.model.User;
import com.travelmind.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserMapper userMapper;

    @GetMapping("/me")
    public Result<User> me() {
        var user = LoginUser.get();
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/me")
    public Result<User> update(@RequestBody User req) {
        req.setId(LoginUser.id());
        req.setPassword(null);
        userMapper.updateById(req);
        var user = userMapper.selectById(req.getId());
        user.setPassword(null);
        return Result.ok(user);
    }
}
