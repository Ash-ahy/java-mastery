package com.mastery.controller;

import com.mastery.common.PageResult;
import com.mastery.common.Result;
import com.mastery.dto.UserDTO;
import com.mastery.dto.UserQueryDTO;
import com.mastery.entity.User;
import com.mastery.service.UserService;
import com.mastery.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping
    public Result<PageResult<User>> list(UserQueryDTO query) {
        return Result.success(userService.queryPage(query));
    }

    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getByIdCached(id));
    }

    /** 查询用户详情（含角色和权限） */
    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/{id}/detail")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.success(userService.getUserVO(id));
    }

    /** 查询用户角色列表 */
    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/{id}/roles")
    public Result<List<String>> roles(@PathVariable Long id) {
        return Result.success(userService.getRoleCodes(id));
    }

    @PreAuthorize("hasAuthority('sys:user:add')")
    @PostMapping
    public Result<User> create(@Validated(UserDTO.Create.class) @RequestBody UserDTO dto) {
        return Result.success(userService.create(dto));
    }

    @PreAuthorize("hasAuthority('sys:user:edit')")
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Validated(UserDTO.Update.class) @RequestBody UserDTO dto) {
        dto.setId(id);
        return Result.success(userService.update(dto));
    }

    /** 分配用户角色 */
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        userService.assignRoles(id, body.get("roleIds"));
        return Result.success();
    }

    @PreAuthorize("hasAuthority('sys:user:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
