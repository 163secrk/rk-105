package com.talentbridge.controller;

import com.talentbridge.common.JwtUtil;
import com.talentbridge.common.Result;
import com.talentbridge.entity.User;
import com.talentbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserWithRoles(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }
}
