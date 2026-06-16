package com.talentbridge.mapper;

import com.talentbridge.entity.Menu;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper {

    @Select("SELECT * FROM sys_menu ORDER BY sort_order")
    List<Menu> findAll();

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_role r ON rm.role_id = r.id " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} " +
            "ORDER BY m.sort_order")
    List<Menu> findMenusByUserId(Long userId);

    @Select("SELECT * FROM sys_menu WHERE id = #{id}")
    Menu findById(Long id);

    @Select("SELECT COUNT(*) FROM sys_menu")
    int count();

    @Insert("INSERT INTO sys_menu (id, menu_name, menu_path, menu_icon, parent_id, sort_order) " +
            "VALUES (#{id}, #{menuName}, #{menuPath}, #{menuIcon}, #{parentId}, #{sortOrder})")
    int insertWithId(Long id, String menuName, String menuPath, String menuIcon, Long parentId, Integer sortOrder);

    @Select("SELECT COUNT(*) FROM sys_role_menu")
    int countRoleMenus();

    @Insert("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (#{roleId}, #{menuId})")
    int insertRoleMenu(Long roleId, Long menuId);
}
