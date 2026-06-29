package com.mastery.controller;

import com.mastery.common.Result;
import com.mastery.entity.Permission;
import com.mastery.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sys/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    public Result<List<Permission>> tree() {
        return Result.success(permissionService.getTree());
    }

    @GetMapping
    public Result<List<Permission>> list() {
        return Result.success(permissionService.listAll());
    }
}
