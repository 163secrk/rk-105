package com.talentbridge.controller;

import com.talentbridge.common.Result;
import com.talentbridge.entity.Talent;
import com.talentbridge.service.TalentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/talent")
@RequiredArgsConstructor
public class TalentController {

    private final TalentService talentService;

    @GetMapping("/list")
    public Result<List<Talent>> list() {
        return Result.success(talentService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Talent> getById(@PathVariable Long id) {
        return Result.success(talentService.findById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Talent talent) {
        talentService.insert(talent);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Talent talent) {
        talentService.update(talent);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        talentService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        talentService.deleteById(id);
        return Result.success();
    }
}
