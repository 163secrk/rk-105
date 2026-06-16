package com.talentbridge.service.impl;

import com.talentbridge.entity.WorkdaySetting;
import com.talentbridge.mapper.WorkdaySettingMapper;
import com.talentbridge.service.WorkdaySettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkdaySettingServiceImpl implements WorkdaySettingService {

    private final WorkdaySettingMapper workdaySettingMapper;

    @Override
    public List<WorkdaySetting> findAll() {
        return workdaySettingMapper.findAll();
    }

    @Override
    public WorkdaySetting findById(Long id) {
        return workdaySettingMapper.findById(id);
    }

    @Override
    public WorkdaySetting findByMonth(String month) {
        return workdaySettingMapper.findByMonth(month);
    }

    @Override
    public WorkdaySetting saveOrUpdate(WorkdaySetting setting) {
        WorkdaySetting existing = workdaySettingMapper.findByMonth(setting.getMonth());
        if (existing != null) {
            existing.setWorkdayCount(setting.getWorkdayCount());
            existing.setRemark(setting.getRemark());
            workdaySettingMapper.update(existing);
            return existing;
        } else {
            workdaySettingMapper.insert(setting);
            return setting;
        }
    }

    @Override
    public void deleteById(Long id) {
        workdaySettingMapper.deleteById(id);
    }
}
