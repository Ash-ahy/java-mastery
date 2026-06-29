package com.mastery.controller;

import com.mastery.common.Result;
import com.mastery.entity.UploadFile;
import com.mastery.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    public Result<UploadFile> upload(Authentication auth, @RequestParam("file") MultipartFile file) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : 1L;
        return Result.success(fileService.upload(file, userId));
    }
    
    @GetMapping("/{id}")
    public Result<byte[]> download(@PathVariable Long id) throws IOException {
        UploadFile uf = fileService.getById(id);
        if (uf == null) return Result.notFound("file not found");
        // 拼接上传根目录
        Path filePath = java.nio.file.Path.of(fileService.getUploadPath(), uf.getFilePath().replaceFirst("^/uploads/", ""));
        byte[] bytes = Files.readAllBytes(filePath);
        return Result.success(bytes);
    }
}
