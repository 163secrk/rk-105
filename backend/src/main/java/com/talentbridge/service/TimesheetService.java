package com.talentbridge.service;

import com.talentbridge.entity.Timesheet;
import com.talentbridge.entity.TimesheetDay;

import java.util.List;
import java.util.Map;

public interface TimesheetService {

    List<Timesheet> findByUserId(Long userId);

    Timesheet findByUserIdAndMonth(Long userId, String month);

    Timesheet findById(Long id);

    List<TimesheetDay> findDaysByTimesheetId(Long timesheetId);

    Timesheet createOrGetDraft(Long userId, String month);

    void saveDays(Long timesheetId, List<TimesheetDay> days);

    void submitTimesheet(Long timesheetId);

    List<Timesheet> findPendingByPmUserId(Long pmUserId);

    List<Timesheet> findAllByPmUserId(Long pmUserId);

    void approve(Long timesheetId, Long approverId, String approverName);

    void reject(Long timesheetId, Long approverId, String approverName, String reason);

    void updateDayStatus(Long timesheetId, String dayDate, String status);
}
