package com.mastery.controller;

import com.mastery.common.Result;
import com.mastery.dto.RoleDTO;
import com.mastery.entity.Role;
import com.mastery.service.RoleService;
import com.mastery.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAuthority('sys:role:list')")
    @GetMapping
    public Result<List<Role>> list() {
        return Result.success(roleService.listAll());
    }

    @PreAuthorize("hasAuthority('sys:role:list')")
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PreAuthorize("hasAuthority('sys:role:add')")
    @PostMapping
    public Result<Role> create(@Valid @RequestBody RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setDescription(dto.getDescription());
        role.setSort(dto.getSort() != null ? dto.getSort() : 0);
        role.setStatus(1);
        return Result.success(roleService.create(role, dto.getPermissionIds()));
    }

    @PreAuthorize("hasAuthority('sys:role:edit')")
    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        Role role = new Role();
        role.setId(id);
        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setDescription(dto.getDescription());
        if (dto.getSort() != null) role.setSort(dto.getSort());
        if (dto.getStatus() != null) role.setStatus(dto.getStatus());
        return Result.success(roleService.update(role, dto.getPermissionIds()));
    }

    @PreAuthorize("hasAuthority('sys:role:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }
}
