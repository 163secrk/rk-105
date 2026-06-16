package com.talentbridge.controller;

import com.talentbridge.common.JwtUtil;
import com.talentbridge.common.Result;
import com.talentbridge.entity.Menu;
import com.talentbridge.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<List<Menu>> getMenuList(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Menu> menus = menuService.findMenusByUserId(userId);
        return Result.success(menus);
    }
}
