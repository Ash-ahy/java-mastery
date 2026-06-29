package com.mastery.service;

import com.mastery.entity.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> getTree();
    List<Permission> listAll();
}
