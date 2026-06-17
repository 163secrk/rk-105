package com.talentbridge.mapper;

import com.talentbridge.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper {

    @Select("SELECT * FROM res_project ORDER BY update_time DESC")
    List<Project> findAll();

    @Select("SELECT * FROM res_project WHERE id = #{id}")
    Project findById(Long id);

    @Insert("INSERT INTO res_project (project_name, client_name, contact_person, contact_phone, " +
            "price_junior, price_middle, price_senior, remark) " +
            "VALUES (#{projectName}, #{clientName}, #{contactPerson}, #{contactPhone}, " +
            "#{priceJunior}, #{priceMiddle}, #{priceSenior}, #{remark})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(Project project);

    @Update("UPDATE res_project SET project_name = #{projectName}, client_name = #{clientName}, " +
            "contact_person = #{contactPerson}, contact_phone = #{contactPhone}, " +
            "price_junior = #{priceJunior}, price_middle = #{priceMiddle}, " +
            "price_senior = #{priceSenior}, remark = #{remark}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(Project project);

    @Delete("DELETE FROM res_project WHERE id = #{id}")
    int deleteById(Long id);
}
