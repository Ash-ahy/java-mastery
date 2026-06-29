package com.mastery.controller;

import com.mastery.common.Result;
import com.mastery.dto.LoginDTO;
import com.mastery.service.AuthService;
import com.mastery.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("login ok", authService.login(dto));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Result.unauthorized("未提供有效的Token");
        }
        String token = auth.substring(7);
        return Result.success(authService.refreshToken(token));
    }
}
