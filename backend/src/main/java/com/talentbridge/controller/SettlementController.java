package com.talentbridge.controller;

import com.talentbridge.common.JwtUtil;
import com.talentbridge.common.Result;
import com.talentbridge.entity.Settlement;
import com.talentbridge.entity.User;
import com.talentbridge.service.SettlementService;
import com.talentbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private boolean isFinance(User user) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> "FINANCE".equals(r.getRoleCode()) || "ADMIN".equals(r.getRoleCode()));
    }

    @GetMapping("/list")
    public Result<List<Settlement>> list(HttpServletRequest request) {
        return Result.success(settlementService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Settlement> getById(@PathVariable Long id, HttpServletRequest request) {
        return Result.success(settlementService.findById(id));
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            return Result.success(settlementService.getSettlementDetail(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/generate")
    public Result<Settlement> generate(@RequestParam String month, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getUserWithRoles(userId);
        if (!isFinance(user)) {
            return Result.error(403, "无权限操作，仅财务人员可生成对账单");
        }
        if (month == null || month.isEmpty()) {
            return Result.error("月份不能为空");
        }
        try {
            return Result.success(settlementService.generateSettlement(month, userId, user.getRealName()));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getUserWithRoles(userId);
        if (!isFinance(user)) {
            return Result.error(403, "无权限操作，仅财务人员可删除对账单");
        }
        try {
            settlementService.deleteById(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
