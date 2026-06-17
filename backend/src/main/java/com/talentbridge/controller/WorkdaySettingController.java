package com.talentbridge.controller;

import com.talentbridge.common.JwtUtil;
import com.talentbridge.common.Result;
import com.talentbridge.entity.User;
import com.talentbridge.entity.WorkdaySetting;
import com.talentbridge.service.UserService;
import com.talentbridge.service.WorkdaySettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/workday")
@RequiredArgsConstructor
public class WorkdaySettingController {

    private final WorkdaySettingService workdaySettingService;
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
    public Result<List<WorkdaySetting>> list(HttpServletRequest request) {
        return Result.success(workdaySettingService.findAll());
    }

    @GetMapping("/{id}")
    public Result<WorkdaySetting> getById(@PathVariable Long id, HttpServletRequest request) {
        return Result.success(workdaySettingService.findById(id));
    }

    @GetMapping("/month/{month}")
    public Result<WorkdaySetting> getByMonth(@PathVariable String month, HttpServletRequest request) {
        return Result.success(workdaySettingService.findByMonth(month));
    }

    @PostMapping("/save")
    public Result<WorkdaySetting> save(@Valid @RequestBody WorkdaySetting setting, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getUserWithRoles(userId);
        if (!isFinance(user)) {
            return Result.error(403, "无权限操作，仅财务人员可设置工作日");
        }
        return Result.success(workdaySettingService.saveOrUpdate(setting));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getUserWithRoles(userId);
        if (!isFinance(user)) {
            return Result.error(403, "无权限操作，仅财务人员可删除设置");
        }
        workdaySettingService.deleteById(id);
        return Result.success();
    }
}
