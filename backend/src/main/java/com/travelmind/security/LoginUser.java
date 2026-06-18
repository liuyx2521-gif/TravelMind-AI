package com.travelmind.security;

import com.travelmind.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class LoginUser {
    private LoginUser() {
    }

    public static Long id() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new IllegalArgumentException("请先登录");
        }
        return user.getId();
    }

    public static User get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new IllegalArgumentException("请先登录");
        }
        return user;
    }

    public static Optional<Long> optionalId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof User user ? Optional.of(user.getId()) : Optional.empty();
    }
}
