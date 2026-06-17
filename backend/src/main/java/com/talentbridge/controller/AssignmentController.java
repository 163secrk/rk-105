package com.talentbridge.controller;

import com.talentbridge.common.Result;
import com.talentbridge.entity.Assignment;
import com.talentbridge.entity.Project;
import com.talentbridge.entity.Talent;
import com.talentbridge.service.AssignmentService;
import com.talentbridge.service.ProjectService;
import com.talentbridge.service.TalentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignment")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final TalentService talentService;
    private final ProjectService projectService;

    @GetMapping("/list")
    public Result<List<Assignment>> list() {
        return Result.success(assignmentService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Assignment> getById(@PathVariable Long id) {
        return Result.success(assignmentService.findById(id));
    }

    @GetMapping("/talent/{talentId}/active")
    public Result<List<Assignment>> getActiveByTalentId(@PathVariable Long talentId) {
        return Result.success(assignmentService.findActiveByTalentId(talentId));
    }

    @GetMapping("/check-conflict")
    public Result<Map<String, Object>> checkConflict(
            @RequestParam Long talentId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long excludeId) {
        boolean conflict = assignmentService.checkTimeConflict(talentId, startDate, endDate, excludeId);
        Map<String, Object> result = new HashMap<>();
        result.put("conflict", conflict);
        if (conflict) {
            List<Assignment> existing = assignmentService.findActiveByTalentId(talentId);
            result.put("conflictingAssignments", existing);
        }
        return Result.success(result);
    }

    @GetMapping("/suggest-price")
    public Result<BigDecimal> suggestPrice(@RequestParam Long projectId, @RequestParam Long talentId) {
        Project project = projectService.findById(projectId);
        Talent talent = talentService.findById(talentId);
        if (project == null || talent == null) {
            return Result.error("项目或人才不存在");
        }
        BigDecimal price = switch (talent.getLevel()) {
            case "JUNIOR" -> project.getPriceJunior();
            case "MIDDLE" -> project.getPriceMiddle();
            case "SENIOR" -> project.getPriceSenior();
            default -> null;
        };
        return Result.success(price);
    }

    @PostMapping
    public Result<Assignment> add(@Valid @RequestBody Assignment assignment) {
        if (assignment.getEndDate().isBefore(assignment.getStartDate())) {
            return Result.error("结束日期不能早于开始日期");
        }
        boolean conflict = assignmentService.checkTimeConflict(
                assignment.getTalentId(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                null
        );
        if (conflict) {
            return Result.error(409, "时间段冲突：该人才在指定时间段内已有其他指派记录");
        }
        Talent talent = talentService.findById(assignment.getTalentId());
        Project project = projectService.findById(assignment.getProjectId());
        if (talent != null) {
            assignment.setTalentName(talent.getName());
            assignment.setTalentLevel(talent.getLevel());
        }
        if (project != null) {
            assignment.setProjectName(project.getProjectName());
        }
        Assignment created = assignmentService.insert(assignment);
        return Result.success(created);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Assignment assignment) {
        if (assignment.getTalentId() != null && assignment.getStartDate() != null && assignment.getEndDate() != null) {
            if (assignment.getEndDate().isBefore(assignment.getStartDate())) {
                return Result.error("结束日期不能早于开始日期");
            }
            boolean conflict = assignmentService.checkTimeConflict(
                    assignment.getTalentId(),
                    assignment.getStartDate(),
                    assignment.getEndDate(),
                    assignment.getId()
            );
            if (conflict) {
                return Result.error(409, "时间段冲突：该人才在指定时间段内已有其他指派记录");
            }
        }
        assignmentService.update(assignment);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assignmentService.deleteById(id);
        return Result.success();
    }
}
