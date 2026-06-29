package com.mastery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mastery.entity.Role;
import com.mastery.vo.RoleVO;

import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> listAll();
    RoleVO getById(Long id);
    Role create(Role role, List<Long> permissionIds);
    Role update(Role role, List<Long> permissionIds);
    void delete(Long id);
}
