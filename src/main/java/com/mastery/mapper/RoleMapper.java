package com.mastery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mastery.entity.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIds(@Param("roleId") Long roleId);

    @Insert("""
        <script>
        INSERT INTO sys_role_permission (role_id, permission_id) VALUES
        <foreach collection='permIds' item='pid' separator=','>
            (#{roleId}, #{pid})
        </foreach>
        </script>
    """)
    int insertRolePermissions(@Param("roleId") Long roleId, @Param("permIds") List<Long> permIds);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Long roleId);
}
