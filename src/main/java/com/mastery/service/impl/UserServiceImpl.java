package com.mastery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mastery.common.PageResult;
import com.mastery.dto.UserDTO;
import com.mastery.dto.UserQueryDTO;
import com.mastery.entity.User;
import com.mastery.exception.BusinessException;
import com.mastery.mapper.UserMapper;
import com.mastery.service.UserService;
import com.mastery.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<User> queryPage(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, query.getKeyword())
                .or().like(User::getPhone, query.getKeyword())
                .or().like(User::getEmail, query.getKeyword()));
        }
        if (query.getStatus() != null) wrapper.eq(User::getStatus, query.getStatus());
        if (query.getGender() != null) wrapper.eq(User::getGender, query.getGender());
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public User getByIdCached(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User create(UserDTO dto) {
        User existing = userMapper.selectByUsername(dto.getUsername());
        if (existing != null) throw new BusinessException(400, "username exists: " + dto.getUsername());
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setGender(dto.getGender());
        user.setStatus(1);
        userMapper.insert(user);

        // 分配角色
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            userMapper.insertUserRoles(user.getId(), dto.getRoleIds());
        }

        log.info("User created: username={}", user.getUsername());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user", key = "#dto.id")
    public User update(UserDTO dto) {
        User user = userMapper.selectById(dto.getId());
        if (user == null) throw new BusinessException(404, "user not found: " + dto.getId());
        if (StringUtils.hasText(dto.getNickname())) user.setNickname(dto.getNickname());
        if (StringUtils.hasText(dto.getPhone())) user.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getEmail())) user.setEmail(dto.getEmail());
        if (dto.getAge() != null) user.setAge(dto.getAge());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(user);

        // 更新角色分配
        if (dto.getRoleIds() != null) {
            userMapper.deleteUserRoles(dto.getId());
            if (!dto.getRoleIds().isEmpty()) {
                userMapper.insertUserRoles(dto.getId(), dto.getRoleIds());
            }
        }

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user", key = "#id")
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "user not found: " + id);
        userMapper.deleteUserRoles(id);
        userMapper.deleteById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        List<String> codes = userMapper.selectRoleCodes(userId);
        return codes != null ? codes : Collections.emptyList();
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        List<String> codes = userMapper.selectPermissionCodes(userId);
        return codes != null ? codes : Collections.emptyList();
    }

    @Override
    public UserVO getUserVO(Long id) {
        User user = Optional.ofNullable(userMapper.selectById(id))
            .orElseThrow(() -> new BusinessException(404, "用户不存在: " + id));
        return UserVO.builder()
            .id(user.getId()).username(user.getUsername()).nickname(user.getNickname())
            .phone(user.getPhone()).email(user.getEmail()).avatar(user.getAvatar())
            .gender(user.getGender()).age(user.getAge()).status(user.getStatus())
            .lastLoginTime(user.getLastLoginTime()).createTime(user.getCreateTime())
            .roles(getRoleCodes(id))
            .permissions(getPermissionCodes(id))
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        if (!userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getId, userId))) {
            throw new BusinessException(404, "用户不存在: " + userId);
        }
        userMapper.deleteUserRoles(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            userMapper.insertUserRoles(userId, roleIds);
        }
        log.info("Roles assigned: userId={}, roleIds={}", userId, roleIds);
    }
}
