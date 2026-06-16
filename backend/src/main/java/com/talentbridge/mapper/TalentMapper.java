package com.talentbridge.mapper;

import com.talentbridge.entity.Talent;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TalentMapper {

    @Select("SELECT * FROM res_talent ORDER BY update_time DESC")
    List<Talent> findAll();

    @Select("SELECT * FROM res_talent WHERE id = #{id}")
    Talent findById(Long id);

    @Insert("INSERT INTO res_talent (name, gender, phone, email, level, monthly_salary, tech_stack, status, remark) " +
            "VALUES (#{name}, #{gender}, #{phone}, #{email}, #{level}, #{monthlySalary}, #{techStack}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Talent talent);

    @Update("UPDATE res_talent SET name = #{name}, gender = #{gender}, phone = #{phone}, email = #{email}, " +
            "level = #{level}, monthly_salary = #{monthlySalary}, tech_stack = #{techStack}, " +
            "status = #{status}, remark = #{remark}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(Talent talent);

    @Update("UPDATE res_talent SET status = #{status}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(Long id, String status);

    @Delete("DELETE FROM res_talent WHERE id = #{id}")
    int deleteById(Long id);
}
