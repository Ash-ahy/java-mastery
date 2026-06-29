package com.mastery.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mastery.entity.UploadFile;
import com.mastery.exception.BusinessException;
import com.mastery.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileService extends ServiceImpl<FileMapper, UploadFile> {
    
    @Value("${app.upload.path:./uploads/}")
    private String uploadPath;

    public String getUploadPath() { return uploadPath; }
    
    public UploadFile upload(MultipartFile file, Long userId) {
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            File dir = new File(uploadPath + dateDir);
            if (!dir.exists()) dir.mkdirs();
            
            String ext = FileUtil.extName(file.getOriginalFilename());
            String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            File dest = new File(dir, newName);
            file.transferTo(dest);
            
            UploadFile uf = new UploadFile();
            uf.setOriginalName(file.getOriginalFilename());
            uf.setFileName(newName);
            uf.setFilePath("/uploads/" + dateDir + "/" + newName);
            uf.setFileSize(file.getSize());
            uf.setFileType(ext);
            // 流式计算MD5，避免大文件OOM
            uf.setMd5(DigestUtil.md5Hex(file.getInputStream()));
            uf.setCreateBy(userId);
            save(uf);
            return uf;
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }
}
