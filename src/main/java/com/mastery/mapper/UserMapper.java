package com.mastery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mastery.dto.UserQueryDTO;
import com.mastery.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 分页条件查询用户
     */
    Page<User> selectPage(Page<User> page, @Param("query") UserQueryDTO query);
    
    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 查询用户的角色编码
     */
    @Select("""
        SELECT r.code FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId}
    """)
    List<String> selectRoleCodes(@Param("userId") Long userId);
    
    /**
     * 查询用户的权限标识
     */
    @Select("""
        SELECT DISTINCT p.code FROM sys_permission p
        INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
        INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND p.code IS NOT NULL AND p.code != ''
    """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);

    /** 批量插入用户-角色关联 */
    @Insert("""
        <script>
        INSERT INTO sys_user_role (user_id, role_id) VALUES
        <foreach collection='roleIds' item='rid' separator=','>
            (#{userId}, #{rid})
        </foreach>
        </script>
    """)
    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /** 删除用户所有角色关联 */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);
}
