package com.talentbridge.mapper;

import com.talentbridge.entity.Role;
import com.talentbridge.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Role> findRolesByUserId(Long userId);

    @Select("SELECT * FROM sys_user")
    List<User> findAll();

    @Insert("INSERT INTO sys_user (username, password, real_name, email, phone, status) " +
            "VALUES (#{username}, #{password}, #{realName}, #{email}, #{phone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE sys_user SET real_name = #{realName}, email = #{email}, phone = #{phone}, " +
            "status = #{status}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_user")
    int count();

    @Select("SELECT COUNT(*) FROM sys_role")
    int countRoles();

    @Insert("INSERT INTO sys_role (id, role_code, role_name, description) VALUES (#{id}, #{roleCode}, #{roleName}, #{description})")
    int insertRoleWithId(Long id, String roleCode, String roleName, String description);

    @Insert("INSERT INTO sys_user (id, username, password, real_name, email, status) " +
            "VALUES (#{id}, #{username}, #{password}, #{realName}, #{email}, #{status})")
    int insertWithId(Long id, String username, String password, String realName, String email, Integer status);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(Long userId, Long roleId);
}
