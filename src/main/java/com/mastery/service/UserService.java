package com.mastery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mastery.common.PageResult;
import com.mastery.dto.UserDTO;
import com.mastery.dto.UserQueryDTO;
import com.mastery.entity.User;
import com.mastery.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /** 分页查询 */
    PageResult<User> queryPage(UserQueryDTO query);
    
    /** 根据ID查询（带缓存） */
    User getByIdCached(Long id);
    
    /** 创建用户 */
    User create(UserDTO dto);
    
    /** 更新用户 */
    User update(UserDTO dto);
    
    /** 删除用户（逻辑删除） */
    void delete(Long id);
    
    /** 根据用户名查询 */
    User getByUsername(String username);

    /** 查询用户的角色编码列表 */
    List<String> getRoleCodes(Long userId);

    /** 查询用户的权限标识列表 */
    List<String> getPermissionCodes(Long userId);

    /** 查询用户详情（含角色和权限） */
    UserVO getUserVO(Long id);

    /** 分配用户角色 */
    void assignRoles(Long userId, List<Long> roleIds);
}
