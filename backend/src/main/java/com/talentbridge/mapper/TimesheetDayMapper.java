package com.talentbridge.mapper;

import com.talentbridge.entity.TimesheetDay;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TimesheetDayMapper {

    @Select("SELECT * FROM ts_timesheet_day WHERE timesheet_id = #{timesheetId} ORDER BY day_date")
    List<TimesheetDay> findByTimesheetId(Long timesheetId);

    @Insert("INSERT INTO ts_timesheet_day (timesheet_id, day_date, status) " +
            "VALUES (#{timesheetId}, #{dayDate}, #{status})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(TimesheetDay day);

    @Delete("DELETE FROM ts_timesheet_day WHERE timesheet_id = #{timesheetId}")
    int deleteByTimesheetId(Long timesheetId);

    @Update("UPDATE ts_timesheet_day SET status = #{status} WHERE timesheet_id = #{timesheetId} AND day_date = #{dayDate}")
    int updateStatus(@Param("timesheetId") Long timesheetId, @Param("dayDate") String dayDate, @Param("status") String status);
}
