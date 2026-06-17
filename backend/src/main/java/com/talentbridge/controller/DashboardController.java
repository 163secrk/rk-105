package com.talentbridge.controller;

import com.talentbridge.common.Result;
import com.talentbridge.dto.DashboardStats;
import com.talentbridge.dto.MonthlyProfit;
import com.talentbridge.dto.ProjectProfit;
import com.talentbridge.entity.Assignment;
import com.talentbridge.entity.Settlement;
import com.talentbridge.entity.SettlementItem;
import com.talentbridge.entity.Talent;
import com.talentbridge.mapper.AssignmentMapper;
import com.talentbridge.mapper.ProjectMapper;
import com.talentbridge.mapper.SettlementItemMapper;
import com.talentbridge.mapper.SettlementMapper;
import com.talentbridge.mapper.TalentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TalentMapper talentMapper;
    private final ProjectMapper projectMapper;
    private final AssignmentMapper assignmentMapper;
    private final SettlementMapper settlementMapper;
    private final SettlementItemMapper settlementItemMapper;

    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        DashboardStats stats = new DashboardStats();

        int activeCount = 0;
        int idleCount = 0;
        List<Talent> talents = talentMapper.findAll();
        for (Talent talent : talents) {
            if ("IDLE".equals(talent.getStatus())) {
                idleCount++;
            } else if ("ASSIGNED".equals(talent.getStatus())) {
                activeCount++;
            }
        }
        stats.setActiveTalentCount(activeCount);
        stats.setIdleTalentCount(idleCount);

        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int newProjectCount = 0;
        var projects = projectMapper.findAll();
        for (var project : projects) {
            if (project.getCreateTime() != null) {
                String projectMonth = project.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                if (currentMonth.equals(projectMonth)) {
                    newProjectCount++;
                }
            }
        }
        stats.setNewProjectCount(newProjectCount);

        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        List<Settlement> settlements = settlementMapper.findByMonth(currentMonth);
        for (Settlement s : settlements) {
            if (s.getTotalAmount() != null) {
                monthlyRevenue = monthlyRevenue.add(s.getTotalAmount());
            }
        }
        stats.setMonthlyRevenue(monthlyRevenue);

        return Result.success(stats);
    }

    @GetMapping("/project-profit")
    public Result<List<ProjectProfit>> getProjectProfit() {
        List<ProjectProfit> result = new ArrayList<>();
        var projects = projectMapper.findAll();
        List<Assignment> allAssignments = assignmentMapper.findAll();
        List<Talent> allTalents = talentMapper.findAll();
        Map<Long, BigDecimal> talentSalaryMap = new HashMap<>();
        for (Talent t : allTalents) {
            if (t.getMonthlySalary() != null) {
                talentSalaryMap.put(t.getId(), t.getMonthlySalary());
            }
        }

        List<SettlementItem> allItems = new ArrayList<>();
        List<Settlement> allSettlements = settlementMapper.findAll();
        for (Settlement s : allSettlements) {
            allItems.addAll(settlementItemMapper.findBySettlementId(s.getId()));
        }

        for (var project : projects) {
            ProjectProfit pp = new ProjectProfit();
            pp.setProjectId(project.getId());
            pp.setProjectName(project.getProjectName());

            BigDecimal totalSettlement = BigDecimal.ZERO;
            for (SettlementItem item : allItems) {
                if (project.getId().equals(item.getProjectId()) && item.getFinalAmount() != null) {
                    totalSettlement = totalSettlement.add(item.getFinalAmount());
                }
            }
            pp.setTotalSettlement(totalSettlement);

            BigDecimal totalSalaryCost = BigDecimal.ZERO;
            for (Assignment a : allAssignments) {
                if (project.getId().equals(a.getProjectId()) && "ACTIVE".equals(a.getStatus())) {
                    BigDecimal salary = talentSalaryMap.get(a.getTalentId());
                    if (salary != null) {
                        totalSalaryCost = totalSalaryCost.add(salary);
                    }
                }
            }
            pp.setTotalSalaryCost(totalSalaryCost);
            pp.setProfit(totalSettlement.subtract(totalSalaryCost));

            result.add(pp);
        }

        result.sort((a, b) -> b.getProfit().compareTo(a.getProfit()));
        return Result.success(result);
    }

    @GetMapping("/monthly-profit")
    public Result<List<MonthlyProfit>> getMonthlyProfit(
            @RequestParam(required = false) String startMonth,
            @RequestParam(required = false) String endMonth,
            @RequestParam(required = false) Long projectId) {

        if (startMonth == null || endMonth == null) {
            YearMonth now = YearMonth.now();
            endMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            startMonth = now.minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<Settlement> allSettlements = settlementMapper.findAll();
        List<SettlementItem> allItems = new ArrayList<>();
        for (Settlement s : allSettlements) {
            allItems.addAll(settlementItemMapper.findBySettlementId(s.getId()));
        }

        List<Assignment> allAssignments = assignmentMapper.findAll();
        List<Talent> allTalents = talentMapper.findAll();
        Map<Long, BigDecimal> talentSalaryMap = new HashMap<>();
        for (Talent t : allTalents) {
            if (t.getMonthlySalary() != null) {
                talentSalaryMap.put(t.getId(), t.getMonthlySalary());
            }
        }

        Map<String, BigDecimal> settlementByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> salaryByMonth = new LinkedHashMap<>();

        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        List<String> months = new ArrayList<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            String m = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            months.add(m);
            settlementByMonth.put(m, BigDecimal.ZERO);
            salaryByMonth.put(m, BigDecimal.ZERO);
            current = current.plusMonths(1);
        }

        for (SettlementItem item : allItems) {
            if (projectId != null && !projectId.equals(item.getProjectId())) {
                continue;
            }
            Settlement s = allSettlements.stream()
                    .filter(x -> x.getId().equals(item.getSettlementId()))
                    .findFirst().orElse(null);
            if (s == null) continue;
            String month = s.getMonth();
            if (settlementByMonth.containsKey(month) && item.getFinalAmount() != null) {
                settlementByMonth.put(month, settlementByMonth.get(month).add(item.getFinalAmount()));
            }
        }

        for (Assignment a : allAssignments) {
            if (projectId != null && !projectId.equals(a.getProjectId())) {
                continue;
            }
            if (!"ACTIVE".equals(a.getStatus()) && !"COMPLETED".equals(a.getStatus())) {
                continue;
            }
            BigDecimal salary = talentSalaryMap.get(a.getTalentId());
            if (salary == null) continue;

            LocalDate startDate = a.getStartDate();
            LocalDate endDate = a.getEndDate() != null ? a.getEndDate() : LocalDate.now();

            for (String m : months) {
                YearMonth ym = YearMonth.parse(m);
                LocalDate monthStart = ym.atDay(1);
                LocalDate monthEnd = ym.atEndOfMonth();
                LocalDate overlapStart = startDate.isAfter(monthStart) ? startDate : monthStart;
                LocalDate overlapEnd = endDate.isBefore(monthEnd) ? endDate : monthEnd;
                if (!overlapStart.isAfter(overlapEnd)) {
                    salaryByMonth.put(m, salaryByMonth.get(m).add(salary));
                }
            }
        }

        List<MonthlyProfit> result = new ArrayList<>();
        for (String m : months) {
            MonthlyProfit mp = new MonthlyProfit();
            mp.setMonth(m);
            BigDecimal settle = settlementByMonth.get(m);
            BigDecimal salary = salaryByMonth.get(m);
            mp.setTotalSettlement(settle);
            mp.setTotalSalaryCost(salary);
            mp.setProfit(settle.subtract(salary));
            result.add(mp);
        }

        return Result.success(result);
    }
}
