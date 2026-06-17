package com.talentbridge.service.impl;

import com.talentbridge.entity.WorkdaySetting;
import com.talentbridge.mapper.WorkdaySettingMapper;
import com.talentbridge.service.WorkdaySettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkdaySettingServiceImpl implements WorkdaySettingService {

    private final WorkdaySettingMapper workdaySettingMapper;

    private static final Set<DayOfWeek> WEEKEND = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private int countWeekdays(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        int count = 0;
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            if (!WEEKEND.contains(ym.atDay(day).getDayOfWeek())) {
                count++;
            }
        }
        return count;
    }

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

    @Override
    public List<WorkdaySetting> generateYearWorkdays(Integer year) {
        List<WorkdaySetting> existing = workdaySettingMapper.findByYear(year);
        Set<String> existingMonths = existing.stream()
                .map(WorkdaySetting::getMonth)
                .collect(Collectors.toSet());

        List<WorkdaySetting> generated = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String monthStr = String.format("%d-%02d", year, month);
            if (existingMonths.contains(monthStr)) {
                generated.add(workdaySettingMapper.findByMonth(monthStr));
                continue;
            }
            int workdayCount = countWeekdays(year, month);
            WorkdaySetting setting = new WorkdaySetting();
            setting.setMonth(monthStr);
            setting.setWorkdayCount(workdayCount);
            setting.setRemark("自动生成（去除周末，请根据法定节假日调整）");
            workdaySettingMapper.insert(setting);
            generated.add(setting);
        }
        return generated;
    }
}
