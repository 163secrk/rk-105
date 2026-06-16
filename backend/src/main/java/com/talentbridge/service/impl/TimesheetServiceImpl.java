package com.talentbridge.service.impl;

import com.talentbridge.entity.Timesheet;
import com.talentbridge.entity.TimesheetDay;
import com.talentbridge.entity.User;
import com.talentbridge.entity.Assignment;
import com.talentbridge.mapper.TimesheetMapper;
import com.talentbridge.mapper.TimesheetDayMapper;
import com.talentbridge.mapper.UserMapper;
import com.talentbridge.mapper.AssignmentMapper;
import com.talentbridge.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetMapper timesheetMapper;
    private final TimesheetDayMapper timesheetDayMapper;
    private final UserMapper userMapper;
    private final AssignmentMapper assignmentMapper;

    @Override
    public List<Timesheet> findByUserId(Long userId) {
        return timesheetMapper.findByUserId(userId);
    }

    @Override
    public Timesheet findByUserIdAndMonth(Long userId, String month) {
        return timesheetMapper.findByUserIdAndMonth(userId, month);
    }

    @Override
    public Timesheet findById(Long id) {
        return timesheetMapper.findById(id);
    }

    @Override
    public List<TimesheetDay> findDaysByTimesheetId(Long timesheetId) {
        return timesheetDayMapper.findByTimesheetId(timesheetId);
    }

    @Override
    @Transactional
    public Timesheet createOrGetDraft(Long userId, String month) {
        Timesheet existing = timesheetMapper.findByUserIdAndMonth(userId, month);
        if (existing != null) {
            return existing;
        }

        User user = userMapper.findById(userId);
        String userName = user != null ? user.getRealName() : "";

        List<Assignment> activeAssignments = assignmentMapper.findActiveByTalentName(userName);
        Long talentId = null;
        String talentName = userName;
        Long projectId = null;
        String projectName = "";

        if (activeAssignments != null && !activeAssignments.isEmpty()) {
            Assignment assignment = activeAssignments.get(0);
            talentId = assignment.getTalentId();
            talentName = assignment.getTalentName();
            projectId = assignment.getProjectId();
            projectName = assignment.getProjectName();
        }

        Timesheet timesheet = new Timesheet();
        timesheet.setUserId(userId);
        timesheet.setUserName(userName);
        timesheet.setTalentId(talentId);
        timesheet.setTalentName(talentName);
        timesheet.setProjectId(projectId != null ? projectId : 0L);
        timesheet.setProjectName(projectName);
        timesheet.setMonth(month);
        timesheet.setStatus("DRAFT");
        timesheetMapper.insert(timesheet);

        YearMonth ym = YearMonth.parse(month);
        int daysInMonth = ym.lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            TimesheetDay day = new TimesheetDay();
            day.setTimesheetId(timesheet.getId());
            day.setDayDate(LocalDate.of(ym.getYear(), ym.getMonthValue(), d));
            day.setStatus("NORMAL");
            timesheetDayMapper.insert(day);
        }

        return timesheet;
    }

    @Override
    @Transactional
    public void saveDays(Long timesheetId, List<TimesheetDay> days) {
        Timesheet timesheet = timesheetMapper.findById(timesheetId);
        if (timesheet == null) {
            throw new RuntimeException("工时单不存在");
        }
        if ("APPROVED".equals(timesheet.getStatus())) {
            throw new RuntimeException("已审批通过的工时单不可修改");
        }
        timesheetDayMapper.deleteByTimesheetId(timesheetId);
        for (TimesheetDay day : days) {
            day.setTimesheetId(timesheetId);
            timesheetDayMapper.insert(day);
        }
    }

    @Override
    public void submitTimesheet(Long timesheetId) {
        Timesheet timesheet = timesheetMapper.findById(timesheetId);
        if (timesheet == null) {
            throw new RuntimeException("工时单不存在");
        }
        if ("APPROVED".equals(timesheet.getStatus())) {
            throw new RuntimeException("已审批通过的工时单不可提交");
        }
        timesheetMapper.updateStatus(timesheetId, "SUBMITTED");
    }

    @Override
    public List<Timesheet> findPendingByPmUserId(Long pmUserId) {
        return timesheetMapper.findPendingByPmUserId(pmUserId, "SUBMITTED");
    }

    @Override
    public List<Timesheet> findAllByPmUserId(Long pmUserId) {
        return timesheetMapper.findByPmUserId(pmUserId);
    }

    @Override
    public void approve(Long timesheetId, Long approverId, String approverName) {
        Timesheet timesheet = timesheetMapper.findById(timesheetId);
        if (timesheet == null) {
            throw new RuntimeException("工时单不存在");
        }
        if (!"SUBMITTED".equals(timesheet.getStatus())) {
            throw new RuntimeException("只能审批待审核的工时单");
        }
        Timesheet update = new Timesheet();
        update.setId(timesheetId);
        update.setStatus("APPROVED");
        update.setApproverId(approverId);
        update.setApproverName(approverName);
        update.setRejectReason(null);
        timesheetMapper.updateApproval(update);
    }

    @Override
    public void reject(Long timesheetId, Long approverId, String approverName, String reason) {
        Timesheet timesheet = timesheetMapper.findById(timesheetId);
        if (timesheet == null) {
            throw new RuntimeException("工时单不存在");
        }
        if (!"SUBMITTED".equals(timesheet.getStatus())) {
            throw new RuntimeException("只能审批待审核的工时单");
        }
        Timesheet update = new Timesheet();
        update.setId(timesheetId);
        update.setStatus("REJECTED");
        update.setApproverId(approverId);
        update.setApproverName(approverName);
        update.setRejectReason(reason);
        timesheetMapper.updateApproval(update);
    }

    @Override
    public void updateDayStatus(Long timesheetId, String dayDate, String status) {
        Timesheet timesheet = timesheetMapper.findById(timesheetId);
        if (timesheet == null) {
            throw new RuntimeException("工时单不存在");
        }
        if ("APPROVED".equals(timesheet.getStatus())) {
            throw new RuntimeException("已审批通过的工时单不可修改");
        }
        timesheetDayMapper.updateStatus(timesheetId, dayDate, status);
    }
}
