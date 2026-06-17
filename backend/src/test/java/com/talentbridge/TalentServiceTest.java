package com.talentbridge;

import com.talentbridge.entity.*;
import com.talentbridge.mapper.*;
import com.talentbridge.service.TalentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TalentServiceTest {

    @Autowired
    private TalentService talentService;

    @Autowired
    private TalentMapper talentMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private TimesheetMapper timesheetMapper;

    @Autowired
    private TimesheetDayMapper timesheetDayMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Talent talentWithPending;
    private Talent talentWithoutPending;
    private Project project;
    private User userWithPending;
    private User userWithoutPending;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        seedTestData();
    }

    private void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM fin_settlement_item");
        jdbcTemplate.update("DELETE FROM fin_settlement");
        jdbcTemplate.update("DELETE FROM ts_timesheet_day");
        jdbcTemplate.update("DELETE FROM ts_timesheet");
        jdbcTemplate.update("DELETE FROM res_assignment");
        jdbcTemplate.update("DELETE FROM fin_workday_setting");
        jdbcTemplate.update("DELETE FROM res_project_pm");
        jdbcTemplate.update("DELETE FROM res_project");
        jdbcTemplate.update("DELETE FROM res_talent");
        jdbcTemplate.update("DELETE FROM sys_user_role");
        jdbcTemplate.update("DELETE FROM sys_user");
    }

    private void seedTestData() {
        project = new Project();
        project.setProjectName("阿里云平台项目");
        project.setClientName("阿里");
        project.setContactPerson("王经理");
        project.setContactPhone("13700137000");
        project.setPriceJunior(new BigDecimal("15000"));
        project.setPriceMiddle(new BigDecimal("21000"));
        project.setPriceSenior(new BigDecimal("30000"));
        projectMapper.insert(project);

        talentWithPending = new Talent();
        talentWithPending.setName("李四");
        talentWithPending.setGender("男");
        talentWithPending.setPhone("13600136000");
        talentWithPending.setEmail("lisi@example.com");
        talentWithPending.setLevel("MIDDLE");
        talentWithPending.setMonthlySalary(new BigDecimal("21000"));
        talentWithPending.setTechStack("Vue,React");
        talentWithPending.setStatus("ASSIGNED");
        talentMapper.insert(talentWithPending);

        talentWithoutPending = new Talent();
        talentWithoutPending.setName("王五");
        talentWithoutPending.setGender("男");
        talentWithoutPending.setPhone("13500135000");
        talentWithoutPending.setEmail("wangwu@example.com");
        talentWithoutPending.setLevel("JUNIOR");
        talentWithoutPending.setMonthlySalary(new BigDecimal("15000"));
        talentWithoutPending.setTechStack("Python");
        talentWithoutPending.setStatus("IDLE");
        talentMapper.insert(talentWithoutPending);

        userWithPending = new User();
        userWithPending.setUsername("lisi_test");
        userWithPending.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyz");
        userWithPending.setRealName("李四");
        userWithPending.setEmail("lisi@example.com");
        userWithPending.setPhone("13600136000");
        userWithPending.setStatus(1);
        userMapper.insert(userWithPending);

        userWithoutPending = new User();
        userWithoutPending.setUsername("wangwu_test");
        userWithoutPending.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyz");
        userWithoutPending.setRealName("王五");
        userWithoutPending.setEmail("wangwu@example.com");
        userWithoutPending.setPhone("13500135000");
        userWithoutPending.setStatus(1);
        userMapper.insert(userWithoutPending);

        Assignment assignment1 = new Assignment();
        assignment1.setTalentId(talentWithPending.getId());
        assignment1.setTalentName("李四");
        assignment1.setProjectId(project.getId());
        assignment1.setProjectName("阿里云平台项目");
        assignment1.setStartDate(LocalDate.of(2026, 6, 1));
        assignment1.setEndDate(LocalDate.of(2026, 12, 31));
        assignment1.setUnitPrice(new BigDecimal("21000"));
        assignment1.setTalentLevel("MIDDLE");
        assignment1.setStatus("ACTIVE");
        assignmentMapper.insert(assignment1);

        Timesheet submittedTimesheet = new Timesheet();
        submittedTimesheet.setUserId(userWithPending.getId());
        submittedTimesheet.setUserName("李四");
        submittedTimesheet.setTalentId(talentWithPending.getId());
        submittedTimesheet.setTalentName("李四");
        submittedTimesheet.setProjectId(project.getId());
        submittedTimesheet.setProjectName("阿里云平台项目");
        submittedTimesheet.setMonth("2026-06");
        submittedTimesheet.setStatus("SUBMITTED");
        timesheetMapper.insert(submittedTimesheet);

        for (int d = 1; d <= 15; d++) {
            TimesheetDay td = new TimesheetDay();
            td.setTimesheetId(submittedTimesheet.getId());
            td.setDayDate(LocalDate.of(2026, 6, d));
            td.setStatus("NORMAL");
            timesheetDayMapper.insert(td);
        }
    }

    @Test
    @DisplayName("离职场景1：李四存在待审批(SUBMITTED)工时单，调用离职接口应被拦截并抛出异常")
    void testResign_WithPendingTimesheet_ShouldBeBlocked() {
        Talent before = talentMapper.findById(talentWithPending.getId());
        assertNotNull(before);
        assertEquals("ASSIGNED", before.getStatus(), "离职前李四状态应为ASSIGNED");

        List<Timesheet> pendingSheets = timesheetMapper.findSubmittedByTalentId(talentWithPending.getId());
        assertFalse(pendingSheets.isEmpty(), "李四应该存在待审批的工时单");
        assertEquals("SUBMITTED", pendingSheets.get(0).getStatus(), "工时单状态应为SUBMITTED");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            talentService.resign(talentWithPending.getId());
        }, "有待审批工时单时调用resign应该抛出RuntimeException");

        assertTrue(ex.getMessage().contains("待审批的工时单") || ex.getMessage().contains("无法办理离职"),
                "异常消息应包含待审批工时单相关提示，实际为：" + ex.getMessage());

        Talent after = talentMapper.findById(talentWithPending.getId());
        assertNotNull(after);
        assertEquals("ASSIGNED", after.getStatus(),
                "离职被拦截后，李四的状态不应被修改，仍应为ASSIGNED");

        System.out.println("[测试结论] 李四存在6月份已提交待审批的工时单，" +
                "调用离职接口被成功拦截，异常信息：" + ex.getMessage() + "，" +
                "人才状态保持不变（仍为" + after.getStatus() + "），离职校验逻辑生效。");
    }

    @Test
    @DisplayName("离职场景2：王五没有待审批工时单，调用离职接口应正常通过，状态变为RESIGNED")
    void testResign_WithoutPendingTimesheet_ShouldPass() {
        Talent before = talentMapper.findById(talentWithoutPending.getId());
        assertNotNull(before);
        assertEquals("IDLE", before.getStatus(), "离职前王五状态应为IDLE");

        List<Timesheet> pendingSheets = timesheetMapper.findSubmittedByTalentId(talentWithoutPending.getId());
        assertTrue(pendingSheets == null || pendingSheets.isEmpty(),
                "王五应该没有待审批的工时单");

        assertDoesNotThrow(() -> {
            talentService.resign(talentWithoutPending.getId());
        }, "没有待审批工时单时调用resign不应抛出异常");

        Talent after = talentMapper.findById(talentWithoutPending.getId());
        assertNotNull(after);
        assertEquals("RESIGNED", after.getStatus(),
                "正常离职后，王五的状态应变为RESIGNED");

        System.out.println("[测试结论] 王五没有任何待审批工时单，" +
                "调用离职接口正常通过，人才状态从 " + before.getStatus() +
                " 变更为 " + after.getStatus() + "，离职流程执行成功。");
    }
}
