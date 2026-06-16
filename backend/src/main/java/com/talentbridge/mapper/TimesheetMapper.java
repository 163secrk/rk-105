package com.talentbridge.mapper;

import com.talentbridge.entity.Timesheet;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TimesheetMapper {

    @Select("SELECT * FROM ts_timesheet WHERE user_id = #{userId} ORDER BY month DESC")
    List<Timesheet> findByUserId(Long userId);

    @Select("SELECT * FROM ts_timesheet WHERE user_id = #{userId} AND month = #{month}")
    Timesheet findByUserIdAndMonth(@Param("userId") Long userId, @Param("month") String month);

    @Select("SELECT * FROM ts_timesheet WHERE id = #{id}")
    Timesheet findById(Long id);

    @Select("SELECT * FROM ts_timesheet WHERE project_id = #{projectId} AND status = #{status} ORDER BY month DESC")
    List<Timesheet> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    @Insert("INSERT INTO ts_timesheet (user_id, user_name, talent_id, talent_name, project_id, project_name, month, status) " +
            "VALUES (#{userId}, #{userName}, #{talentId}, #{talentName}, #{projectId}, #{projectName}, #{month}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Timesheet timesheet);

    @Update("UPDATE ts_timesheet SET status = #{status}, approver_id = #{approverId}, " +
            "approver_name = #{approverName}, approve_time = CURRENT_TIMESTAMP, " +
            "reject_reason = #{rejectReason}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateApproval(Timesheet timesheet);

    @Update("UPDATE ts_timesheet SET status = #{status}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT DISTINCT t.* FROM ts_timesheet t " +
            "INNER JOIN res_project_pm ppm ON t.project_id = ppm.project_id " +
            "WHERE ppm.pm_user_id = #{pmUserId} AND t.status = #{status} ORDER BY t.month DESC")
    List<Timesheet> findPendingByPmUserId(@Param("pmUserId") Long pmUserId, @Param("status") String status);

    @Select("SELECT DISTINCT t.* FROM ts_timesheet t " +
            "INNER JOIN res_project_pm ppm ON t.project_id = ppm.project_id " +
            "WHERE ppm.pm_user_id = #{pmUserId} ORDER BY t.month DESC")
    List<Timesheet> findByPmUserId(@Param("pmUserId") Long pmUserId);
}
