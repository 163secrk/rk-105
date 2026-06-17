package com.talentbridge.mapper;

import com.talentbridge.entity.WorkdaySetting;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WorkdaySettingMapper {

    @Select("SELECT * FROM fin_workday_setting ORDER BY month DESC")
    List<WorkdaySetting> findAll();

    @Select("SELECT * FROM fin_workday_setting WHERE id = #{id}")
    WorkdaySetting findById(Long id);

    @Select("SELECT * FROM fin_workday_setting WHERE month = #{month}")
    WorkdaySetting findByMonth(String month);

    @Insert("INSERT INTO fin_workday_setting (month, workday_count, remark) " +
            "VALUES (#{month}, #{workdayCount}, #{remark})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(WorkdaySetting setting);

    @Update("UPDATE fin_workday_setting SET workday_count = #{workdayCount}, " +
            "remark = #{remark}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(WorkdaySetting setting);

    @Select("SELECT * FROM fin_workday_setting WHERE month LIKE CONCAT(#{year}, '-%') ORDER BY month")
    List<WorkdaySetting> findByYear(Integer year);

    @Delete("DELETE FROM fin_workday_setting WHERE id = #{id}")
    int deleteById(Long id);
}
