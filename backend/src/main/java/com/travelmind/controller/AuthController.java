package com.travelmind.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.travelmind.common.Result;
import com.travelmind.mapper.UserMapper;
import com.travelmind.model.User;
import com.travelmind.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public record RegisterReq(@NotBlank String username, @Email String email, @NotBlank String password) {
    }

    public record LoginReq(@NotBlank String account, @NotBlank String password) {
    }

    public record LoginResp(String token, User user) {
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterReq req) {
        var exists = userMapper.exists(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, req.username())
                .or()
                .eq(User::getEmail, req.email()));
        if (exists) {
            throw new IllegalArgumentException("用户名或邮箱已存在");
        }
        var user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        userMapper.insert(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req) {
        var user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, req.account())
                .or()
                .eq(User::getEmail, req.account()));
        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        var token = jwtUtil.create(user.getId(), user.getUsername());
        user.setPassword(null);
        return Result.ok(new LoginResp(token, user));
    }
}
