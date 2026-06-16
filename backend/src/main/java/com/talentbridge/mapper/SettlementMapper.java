package com.talentbridge.mapper;

import com.talentbridge.entity.Settlement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SettlementMapper {

    @Select("SELECT * FROM fin_settlement ORDER BY create_time DESC")
    List<Settlement> findAll();

    @Select("SELECT * FROM fin_settlement WHERE id = #{id}")
    Settlement findById(Long id);

    @Select("SELECT * FROM fin_settlement WHERE settlement_no = #{settlementNo}")
    Settlement findBySettlementNo(String settlementNo);

    @Select("SELECT * FROM fin_settlement WHERE month = #{month}")
    List<Settlement> findByMonth(String month);

    @Insert("INSERT INTO fin_settlement (settlement_no, month, workday_count, total_amount, " +
            "item_count, status, creator_id, creator_name) VALUES (#{settlementNo}, #{month}, " +
            "#{workdayCount}, #{totalAmount}, #{itemCount}, #{status}, #{creatorId}, #{creatorName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Settlement settlement);

    @Update("UPDATE fin_settlement SET total_amount = #{totalAmount}, item_count = #{itemCount}, " +
            "update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateTotals(Settlement settlement);

    @Delete("DELETE FROM fin_settlement WHERE id = #{id}")
    int deleteById(Long id);
}
