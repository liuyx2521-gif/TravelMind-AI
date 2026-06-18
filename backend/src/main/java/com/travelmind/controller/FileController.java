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

    public FileController(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
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

        var baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        var url = baseUrl + "/uploads/" + bucket + "/" + objectName.replace("\\", "/");
        return Result.ok(Map.of("url", url, "objectName", objectName));
    }
}
