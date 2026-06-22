package com.travelmind.controller;

import com.travelmind.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final Path uploadDir;
    private final String publicBaseUrl;

    public FileController(@Value("${app.upload.dir:uploads}") String uploadDir,
                          @Value("${app.public-base-url:}") String publicBaseUrl) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.replaceAll("/+$", "");
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestPart MultipartFile file,
                                              @RequestParam(defaultValue = "travelmind") String bucket,
                                              HttpServletRequest request) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("文件不能为空");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("文件不能超过10MB");

        var original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        var suffix = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        var objectName = LocalDate.now() + "/" + UUID.randomUUID() + suffix;
        var target = uploadDir.resolve(bucket).resolve(objectName).normalize();
        if (!target.startsWith(uploadDir)) throw new IllegalArgumentException("非法文件路径");

        Files.createDirectories(target.getParent());
        file.transferTo(target);

        var baseUrl = baseUrl(request);
        var url = baseUrl + "/uploads/" + bucket + "/" + objectName.replace("\\", "/");
        return Result.ok(Map.of("url", url, "objectName", objectName));
    }

    private String baseUrl(HttpServletRequest request) {
        if (!publicBaseUrl.isBlank()) return publicBaseUrl;
        var proto = header(request, "X-Forwarded-Proto", request.getScheme());
        var host = header(request, "X-Forwarded-Host", request.getHeader("Host"));
        if (host != null && !host.isBlank()) return proto + "://" + host;
        var port = request.getServerPort();
        var defaultPort = ("https".equalsIgnoreCase(proto) && port == 443) || ("http".equalsIgnoreCase(proto) && port == 80);
        return proto + "://" + request.getServerName() + (defaultPort ? "" : ":" + port);
    }

    private String header(HttpServletRequest request, String name, String fallback) {
        var value = request.getHeader(name);
        return value == null || value.isBlank() ? fallback : value.split(",", 2)[0].trim();
    }
}
