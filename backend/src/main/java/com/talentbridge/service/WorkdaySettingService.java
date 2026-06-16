package com.talentbridge.service;

import com.talentbridge.entity.WorkdaySetting;
import java.util.List;

public interface WorkdaySettingService {
    List<WorkdaySetting> findAll();
    WorkdaySetting findById(Long id);
    WorkdaySetting findByMonth(String month);
    WorkdaySetting saveOrUpdate(WorkdaySetting setting);
    void deleteById(Long id);
}
