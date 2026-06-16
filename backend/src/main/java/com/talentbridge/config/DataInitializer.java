package com.talentbridge.config;

import com.talentbridge.entity.Menu;
import com.talentbridge.entity.Role;
import com.talentbridge.entity.User;
import com.talentbridge.mapper.MenuMapper;
import com.talentbridge.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final MenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initMenus();
        initUsers();
        initRoleMenus();
        log.info("数据初始化完成");
    }

    private void initRoles() {
        if (userMapper.countRoles() > 0) {
            return;
        }
        userMapper.insertRoleWithId(1L, "ADMIN", "管理员", "系统管理员");
        userMapper.insertRoleWithId(2L, "HR", "HR", "人力资源");
        userMapper.insertRoleWithId(3L, "FINANCE", "财务", "财务人员");
        userMapper.insertRoleWithId(4L, "PM", "客户PM", "客户项目经理");
        userMapper.insertRoleWithId(5L, "EMPLOYEE", "外派员工", "外派员工");
        log.info("角色数据初始化完成");
    }

    private void initMenus() {
        if (menuMapper.count() > 0) {
            return;
        }
        menuMapper.insertWithId(1L, "工作台", "/dashboard", "Dashboard", 0L, 1);
        menuMapper.insertWithId(2L, "资源管理", "/resource", "User", 0L, 2);
        menuMapper.insertWithId(3L, "项目池", "/project", "Briefcase", 0L, 3);
        menuMapper.insertWithId(4L, "指派中心", "/assignment", "ClipboardList", 0L, 4);
        menuMapper.insertWithId(5L, "报工审批", "/timesheet", "FileText", 0L, 5);
        menuMapper.insertWithId(6L, "财务结算", "/finance", "DollarSign", 0L, 6);
        menuMapper.insertWithId(7L, "系统设置", "/settings", "Settings", 0L, 7);
        log.info("菜单数据初始化完成");
    }

    private void initUsers() {
        if (userMapper.count() > 0) {
            return;
        }
        String encodedPassword = passwordEncoder.encode("123456");

        userMapper.insertWithId(1L, "admin", encodedPassword, "管理员", "admin@talentbridge.com", 1);
        userMapper.insertUserRole(1L, 1L);

        userMapper.insertWithId(2L, "hr", encodedPassword, "HR专员", "hr@talentbridge.com", 1);
        userMapper.insertUserRole(2L, 2L);

        userMapper.insertWithId(3L, "finance", encodedPassword, "财务专员", "finance@talentbridge.com", 1);
        userMapper.insertUserRole(3L, 3L);

        userMapper.insertWithId(4L, "pm", encodedPassword, "客户PM", "pm@talentbridge.com", 1);
        userMapper.insertUserRole(4L, 4L);

        userMapper.insertWithId(5L, "employee", encodedPassword, "外派员工", "employee@talentbridge.com", 1);
        userMapper.insertUserRole(5L, 5L);

        log.info("用户数据初始化完成");
    }

    private void initRoleMenus() {
        if (menuMapper.countRoleMenus() > 0) {
            return;
        }

        menuMapper.insertRoleMenu(1L, 1L);
        menuMapper.insertRoleMenu(1L, 2L);
        menuMapper.insertRoleMenu(1L, 3L);
        menuMapper.insertRoleMenu(1L, 4L);
        menuMapper.insertRoleMenu(1L, 5L);
        menuMapper.insertRoleMenu(1L, 6L);
        menuMapper.insertRoleMenu(1L, 7L);

        menuMapper.insertRoleMenu(2L, 1L);
        menuMapper.insertRoleMenu(2L, 2L);
        menuMapper.insertRoleMenu(2L, 3L);
        menuMapper.insertRoleMenu(2L, 4L);

        menuMapper.insertRoleMenu(3L, 1L);
        menuMapper.insertRoleMenu(3L, 5L);
        menuMapper.insertRoleMenu(3L, 6L);

        menuMapper.insertRoleMenu(4L, 1L);
        menuMapper.insertRoleMenu(4L, 3L);
        menuMapper.insertRoleMenu(4L, 5L);

        menuMapper.insertRoleMenu(5L, 1L);
        menuMapper.insertRoleMenu(5L, 5L);

        log.info("角色菜单关联数据初始化完成");
    }
}
