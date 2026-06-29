package com.mastery.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RoleVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private Integer sort;
    private List<Long> permissionIds;
}
