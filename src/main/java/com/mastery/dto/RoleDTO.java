package com.mastery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 角色创建/更新请求
 */
@Data
public class RoleDTO {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    private String code;

    private String description;

    private Integer sort;

    private Integer status;

    private List<Long> permissionIds;
}
