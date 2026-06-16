package com.talentbridge.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ProjectPmMapper {

    @Insert("INSERT INTO res_project_pm (project_id, pm_user_id) VALUES (#{projectId}, #{pmUserId})")
    int insert(@Param("projectId") Long projectId, @Param("pmUserId") Long pmUserId);

    @Select("SELECT COUNT(*) FROM res_project_pm")
    int count();
}
