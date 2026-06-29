package com.mastery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class UploadFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String originalName;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String md5;
    private Long createBy;
    private LocalDateTime createTime;
}
