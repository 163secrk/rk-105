package com.talentbridge.mapper;

import com.talentbridge.entity.Assignment;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AssignmentMapper {

    @Select("SELECT * FROM res_assignment ORDER BY create_time DESC")
    List<Assignment> findAll();

    @Select("SELECT * FROM res_assignment WHERE id = #{id}")
    Assignment findById(Long id);

    @Select("SELECT * FROM res_assignment WHERE talent_id = #{talentId} AND status = 'ACTIVE'")
    List<Assignment> findActiveByTalentId(Long talentId);

    @Select("SELECT * FROM res_assignment WHERE talent_id = #{talentId} AND status = 'ACTIVE' AND id != #{excludeId}")
    List<Assignment> findActiveByTalentIdExclude(@Param("talentId") Long talentId, @Param("excludeId") Long excludeId);

    @Insert("INSERT INTO res_assignment (talent_id, talent_name, project_id, project_name, start_date, end_date, unit_price, talent_level, status, remark) " +
            "VALUES (#{talentId}, #{talentName}, #{projectId}, #{projectName}, #{startDate}, #{endDate}, #{unitPrice}, #{talentLevel}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assignment assignment);

    @Update("UPDATE res_assignment SET talent_id = #{talentId}, talent_name = #{talentName}, project_id = #{projectId}, " +
            "project_name = #{projectName}, start_date = #{startDate}, end_date = #{endDate}, unit_price = #{unitPrice}, " +
            "talent_level = #{talentLevel}, status = #{status}, remark = #{remark}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(Assignment assignment);

    @Delete("DELETE FROM res_assignment WHERE id = #{id}")
    int deleteById(Long id);
}
