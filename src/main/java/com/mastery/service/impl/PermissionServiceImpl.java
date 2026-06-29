package com.mastery.service.impl;

import com.mastery.entity.Permission;
import com.mastery.mapper.PermissionMapper;
import com.mastery.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<Permission> listAll() {
        return permissionMapper.selectAll();
    }

    @Override
    public List<Permission> getTree() {
        List<Permission> all = permissionMapper.selectAll();
        Map<Long, List<Permission>> byParent = all.stream()
            .collect(Collectors.groupingBy(p -> p.getParentId() != null ? p.getParentId() : 0L));

        List<Permission> roots = byParent.getOrDefault(0L, new ArrayList<>());
        for (Permission root : roots) {
            buildChildren(root, byParent);
        }
        return roots;
    }

    private void buildChildren(Permission parent, Map<Long, List<Permission>> byParent) {
        List<Permission> children = byParent.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        for (Permission child : children) {
            buildChildren(child, byParent);
        }
    }
}
