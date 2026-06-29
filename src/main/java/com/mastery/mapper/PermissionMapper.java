package com.mastery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mastery.entity.Permission;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT * FROM sys_permission ORDER BY sort")
    List<Permission> selectAll();
}
