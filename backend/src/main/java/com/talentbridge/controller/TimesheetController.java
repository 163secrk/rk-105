package com.talentbridge.controller;

import com.talentbridge.common.JwtUtil;
import com.talentbridge.common.Result;
import com.talentbridge.entity.Timesheet;
import com.talentbridge.entity.TimesheetDay;
import com.talentbridge.entity.User;
import com.talentbridge.service.TimesheetService;
import com.talentbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timesheet")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private User getCurrentUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return userService.getUserWithRoles(userId);
    }

    private boolean isEmployee(User user) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> "EMPLOYEE".equals(r.getRoleCode()));
    }

    private boolean isPm(User user) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> "PM".equals(r.getRoleCode()));
    }

    @GetMapping("/my-list")
    public Result<List<Timesheet>> myTimesheetList(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(timesheetService.findByUserId(userId));
    }

    @GetMapping("/month")
    public Result<Map<String, Object>> getMonthTimesheet(
            @RequestParam String month,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Timesheet timesheet = timesheetService.findByUserIdAndMonth(userId, month);
        List<TimesheetDay> days = null;
        if (timesheet != null) {
            days = timesheetService.findDaysByTimesheetId(timesheet.getId());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("timesheet", timesheet);
        result.put("days", days);
        return Result.success(result);
    }

    @PostMapping("/init")
    public Result<Map<String, Object>> initMonthTimesheet(
            @RequestParam String month,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Timesheet timesheet = timesheetService.createOrGetDraft(userId, month);
        List<TimesheetDay> days = timesheetService.findDaysByTimesheetId(timesheet.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("timesheet", timesheet);
        result.put("days", days);
        return Result.success(result);
    }

    @PutMapping("/day-status")
    public Result<Void> updateDayStatus(
            @RequestParam Long timesheetId,
            @RequestParam String dayDate,
            @RequestParam String status,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Timesheet timesheet = timesheetService.findById(timesheetId);
        if (timesheet == null) {
            return Result.error("工时单不存在");
        }
        if (!timesheet.getUserId().equals(userId)) {
            return Result.error("无权操作此工时单");
        }
        timesheetService.updateDayStatus(timesheetId, dayDate, status);
        return Result.success();
    }

    @PostMapping("/submit/{id}")
    public Result<Void> submitTimesheet(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Timesheet timesheet = timesheetService.findById(id);
        if (timesheet == null) {
            return Result.error("工时单不存在");
        }
        if (!timesheet.getUserId().equals(userId)) {
            return Result.error("无权操作此工时单");
        }
        try {
            timesheetService.submitTimesheet(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/pending-list")
    public Result<List<Timesheet>> pendingList(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(timesheetService.findPendingByPmUserId(userId));
    }

    @GetMapping("/pm-list")
    public Result<List<Timesheet>> pmAllList(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(timesheetService.findAllByPmUserId(userId));
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id, HttpServletRequest request) {
        Timesheet timesheet = timesheetService.findById(id);
        if (timesheet == null) {
            return Result.error("工时单不存在");
        }
        List<TimesheetDay> days = timesheetService.findDaysByTimesheetId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("timesheet", timesheet);
        result.put("days", days);
        return Result.success(result);
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = getCurrentUser(request);
        try {
            timesheetService.approve(id, userId, user.getRealName());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reject/{id}")
    public Result<Void> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = getCurrentUser(request);
        String reason = body != null ? body.get("reason") : "";
        try {
            timesheetService.reject(id, userId, user.getRealName(), reason);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
