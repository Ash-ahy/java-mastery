package com.mastery.dto;

import lombok.Data;

/**
 * 用户分页查询条件
 */
@Data
public class UserQueryDTO {
    
    private String keyword;
    
    private Integer status;
    
    private Integer gender;
    
    private String startDate;
    
    private String endDate;
    
    /** 页码（从1开始） */
    private Integer page = 1;
    
    /** 每页大小 */
    private Integer size = 10;
}
