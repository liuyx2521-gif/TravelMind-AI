package com.travelmind.service;

import com.travelmind.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate redisTemplate;

    public void check(String bucket, int maxRequests, Duration window) {
        if (maxRequests <= 0 || window.isZero() || window.isNegative()) return;
        var windowSeconds = Math.max(1, window.toSeconds());
        var key = "rate:%s:%s:%d".formatted(bucket, subject(), System.currentTimeMillis() / 1000 / windowSeconds);
        try {
            var count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, window.plus(window));
            }
            if (count != null && count > maxRequests) {
                throw new IllegalArgumentException("请求过于频繁，请稍后再试");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException ignored) {
        }
    }

    private String subject() {
        return LoginUser.optionalId().map(id -> "user:" + id).orElseGet(() -> "ip:" + clientIp());
    }

    private String clientIp() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) return "unknown";
        HttpServletRequest request = servletAttrs.getRequest();
        var forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        var realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp;
    }
}
