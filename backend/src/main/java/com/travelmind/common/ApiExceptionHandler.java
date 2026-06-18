package com.travelmind.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> valid(MethodArgumentNotValidException e) {
        var msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(x -> x.getField() + " " + x.getDefaultMessage())
                .orElse("参数错误");
        return Result.fail(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> badRequest(IllegalArgumentException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> error(Exception e) {
        return Result.fail(e.getMessage());
    }
}
