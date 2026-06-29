package com.mastery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mastery.entity.Role;
import com.mastery.exception.BusinessException;
import com.mastery.mapper.RoleMapper;
import com.mastery.service.RoleService;
import com.mastery.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public List<Role> listAll() {
        return roleMapper.selectList(null);
    }

    @Override
    public RoleVO getById(Long id) {
        Role role = Optional.ofNullable(roleMapper.selectById(id))
            .orElseThrow(() -> new BusinessException(404, "角色不存在: " + id));
        List<Long> permIds = roleMapper.selectPermissionIds(id);
        return RoleVO.builder()
            .id(role.getId()).name(role.getName()).code(role.getCode())
            .description(role.getDescription()).status(role.getStatus()).sort(role.getSort())
            .permissionIds(permIds)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role create(Role role, List<Long> permissionIds) {
        roleMapper.insert(role);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            roleMapper.insertRolePermissions(role.getId(), permissionIds);
        }
        log.info("Role created: name={}", role.getName());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role update(Role role, List<Long> permissionIds) {
        roleMapper.updateById(role);
        roleMapper.deleteRolePermissions(role.getId());
        if (permissionIds != null && !permissionIds.isEmpty()) {
            roleMapper.insertRolePermissions(role.getId(), permissionIds);
        }
        log.info("Role updated: id={}", role.getId());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        roleMapper.deleteRolePermissions(id);
        roleMapper.deleteById(id);
        log.info("Role deleted: id={}", id);
    }
}
