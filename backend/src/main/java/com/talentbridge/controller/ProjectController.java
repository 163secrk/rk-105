package com.talentbridge.controller;

import com.talentbridge.common.Result;
import com.talentbridge.entity.Project;
import com.talentbridge.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/list")
    public Result<List<Project>> list() {
        return Result.success(projectService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.findById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody Project project) {
        projectService.insert(project);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Project project) {
        projectService.update(project);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.deleteById(id);
        return Result.success();
    }
}
