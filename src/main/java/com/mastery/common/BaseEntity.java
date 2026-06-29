package com.mastery.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类 - 所有数据库实体继承此类
 */
@Data
public class BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 更新人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /** 逻辑删除 0未删 1已删 */
    @TableLogic
    private Integer deleted;
}
