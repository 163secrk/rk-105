package com.talentbridge.service.impl;

import com.talentbridge.entity.*;
import com.talentbridge.mapper.*;
import com.talentbridge.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementMapper settlementMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final WorkdaySettingMapper workdaySettingMapper;
    private final TimesheetMapper timesheetMapper;
    private final TimesheetDayMapper timesheetDayMapper;
    private final AssignmentMapper assignmentMapper;

    @Override
    public List<Settlement> findAll() {
        return settlementMapper.findAll();
    }

    @Override
    public Settlement findById(Long id) {
        return settlementMapper.findById(id);
    }

    @Override
    public List<Settlement> findByMonth(String month) {
        return settlementMapper.findByMonth(month);
    }

    @Override
    @Transactional
    public Settlement generateSettlement(String month, Long creatorId, String creatorName) {
        List<Settlement> existing = settlementMapper.findByMonth(month);
        if (existing != null && !existing.isEmpty()) {
            throw new RuntimeException(month + " 月份已生成对账单，请勿重复生成");
        }

        WorkdaySetting workdaySetting = workdaySettingMapper.findByMonth(month);
        if (workdaySetting == null || workdaySetting.getWorkdayCount() == null || workdaySetting.getWorkdayCount() <= 0) {
            throw new RuntimeException("请先设置 " + month + " 月份的法定工作日天数");
        }
        int workdayCount = workdaySetting.getWorkdayCount();

        List<Timesheet> approvedTimesheets = timesheetMapper.findApprovedByMonth(month);
        if (approvedTimesheets == null || approvedTimesheets.isEmpty()) {
            throw new RuntimeException(month + " 月份没有已通过审批的工时单");
        }

        String settlementNo = "STL" + month.replace("-", "") +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));

        Settlement settlement = new Settlement();
        settlement.setSettlementNo(settlementNo);
        settlement.setMonth(month);
        settlement.setWorkdayCount(workdayCount);
        settlement.setTotalAmount(BigDecimal.ZERO);
        settlement.setItemCount(0);
        settlement.setStatus("GENERATED");
        settlement.setCreatorId(creatorId);
        settlement.setCreatorName(creatorName);
        settlementMapper.insert(settlement);

        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;

        for (Timesheet timesheet : approvedTimesheets) {
            List<TimesheetDay> days = timesheetDayMapper.findByTimesheetId(timesheet.getId());
            int actualAttendanceDays = 0;
            int overtimeDays = 0;
            if (days != null) {
                for (TimesheetDay day : days) {
                    if ("NORMAL".equals(day.getStatus())) {
                        actualAttendanceDays++;
                    } else if ("OVERTIME".equals(day.getStatus())) {
                        overtimeDays++;
                        actualAttendanceDays++;
                    }
                }
            }

            BigDecimal unitPrice = BigDecimal.ZERO;
            if (timesheet.getTalentId() != null) {
                List<Assignment> assignments = assignmentMapper.findActiveByTalentId(timesheet.getTalentId());
                if (assignments != null && !assignments.isEmpty()) {
                    for (Assignment a : assignments) {
                        if (a.getProjectId().equals(timesheet.getProjectId())) {
                            unitPrice = a.getUnitPrice() != null ? a.getUnitPrice() : BigDecimal.ZERO;
                            break;
                        }
                    }
                    if (unitPrice.compareTo(BigDecimal.ZERO) == 0) {
                        unitPrice = assignments.get(0).getUnitPrice() != null ? assignments.get(0).getUnitPrice() : BigDecimal.ZERO;
                    }
                }
            }

            BigDecimal dailyPrice = unitPrice.divide(BigDecimal.valueOf(workdayCount), 4, RoundingMode.HALF_UP);
            BigDecimal baseAmount = dailyPrice.multiply(BigDecimal.valueOf(actualAttendanceDays))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal overtimeDailyPrice = dailyPrice;
            BigDecimal overtimeAmount = overtimeDailyPrice.multiply(BigDecimal.valueOf(overtimeDays))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal finalAmount = baseAmount.add(overtimeAmount).setScale(2, RoundingMode.HALF_UP);

            String calcDetail = String.format(
                    "基础金额 = %.2f ÷ %d × %d = %.2f；加班金额 = %.2f × %d = %.2f；合计 = %.2f",
                    unitPrice, workdayCount, actualAttendanceDays, baseAmount,
                    overtimeDailyPrice.setScale(2, RoundingMode.HALF_UP), overtimeDays, overtimeAmount,
                    finalAmount);

            SettlementItem item = new SettlementItem();
            item.setSettlementId(settlement.getId());
            item.setTimesheetId(timesheet.getId());
            item.setUserId(timesheet.getUserId());
            item.setUserName(timesheet.getUserName());
            item.setTalentId(timesheet.getTalentId());
            item.setTalentName(timesheet.getTalentName());
            item.setProjectId(timesheet.getProjectId());
            item.setProjectName(timesheet.getProjectName());
            item.setUnitPriceSnapshot(unitPrice);
            item.setWorkdayCountSnapshot(workdayCount);
            item.setActualAttendanceDays(actualAttendanceDays);
            item.setOvertimeDays(overtimeDays);
            item.setOvertimeAmount(overtimeAmount);
            item.setBaseAmount(baseAmount);
            item.setFinalAmount(finalAmount);
            item.setCalcDetail(calcDetail);
            settlementItemMapper.insert(item);

            totalAmount = totalAmount.add(finalAmount);
            itemCount++;
        }

        settlement.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        settlement.setItemCount(itemCount);
        settlementMapper.updateTotals(settlement);

        return settlementMapper.findById(settlement.getId());
    }

    @Override
    public Map<String, Object> getSettlementDetail(Long id) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            throw new RuntimeException("结算单不存在");
        }
        List<SettlementItem> items = settlementItemMapper.findBySettlementId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("settlement", settlement);
        result.put("items", items);
        return result;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        settlementItemMapper.deleteBySettlementId(id);
        settlementMapper.deleteById(id);
    }
}
