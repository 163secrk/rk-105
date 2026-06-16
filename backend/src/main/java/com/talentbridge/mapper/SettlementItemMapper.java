package com.talentbridge.mapper;

import com.talentbridge.entity.SettlementItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SettlementItemMapper {

    @Select("SELECT * FROM fin_settlement_item WHERE settlement_id = #{settlementId} ORDER BY id")
    List<SettlementItem> findBySettlementId(Long settlementId);

    @Select("SELECT * FROM fin_settlement_item WHERE id = #{id}")
    SettlementItem findById(Long id);

    @Insert("INSERT INTO fin_settlement_item (settlement_id, timesheet_id, user_id, user_name, " +
            "talent_id, talent_name, project_id, project_name, unit_price_snapshot, " +
            "workday_count_snapshot, actual_attendance_days, overtime_days, overtime_amount, " +
            "base_amount, final_amount, calc_detail) VALUES (#{settlementId}, #{timesheetId}, " +
            "#{userId}, #{userName}, #{talentId}, #{talentName}, #{projectId}, #{projectName}, " +
            "#{unitPriceSnapshot}, #{workdayCountSnapshot}, #{actualAttendanceDays}, " +
            "#{overtimeDays}, #{overtimeAmount}, #{baseAmount}, #{finalAmount}, #{calcDetail})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SettlementItem item);

    @Delete("DELETE FROM fin_settlement_item WHERE settlement_id = #{settlementId}")
    int deleteBySettlementId(Long settlementId);
}
