package com.talentbridge;

import com.talentbridge.entity.*;
import com.talentbridge.mapper.*;
import com.talentbridge.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;

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
    private WorkdaySettingMapper workdaySettingMapper;

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_MONTH = "2026-06";
    private static final int TOTAL_WORKDAYS = 21;
    private static final int ACTUAL_ATTENDANCE = 15;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("21000.00");

    private Talent talentZhangsan;
    private Project projectHuawei;
    private User userZhangsan;
    private Assignment assignment;
    private Timesheet timesheet;

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
        WorkdaySetting workdaySetting = new WorkdaySetting();
        workdaySetting.setMonth(TEST_MONTH);
        workdaySetting.setWorkdayCount(TOTAL_WORKDAYS);
        workdaySetting.setRemark("6月法定工作日");
        workdaySettingMapper.insert(workdaySetting);

        talentZhangsan = new Talent();
        talentZhangsan.setName("张三");
        talentZhangsan.setGender("男");
        talentZhangsan.setPhone("13800138000");
        talentZhangsan.setEmail("zhangsan@example.com");
        talentZhangsan.setLevel("MIDDLE");
        talentZhangsan.setMonthlySalary(UNIT_PRICE);
        talentZhangsan.setTechStack("Java,Spring");
        talentZhangsan.setStatus("ASSIGNED");
        talentMapper.insert(talentZhangsan);

        projectHuawei = new Project();
        projectHuawei.setProjectName("华为云平台项目");
        projectHuawei.setClientName("华为");
        projectHuawei.setContactPerson("李经理");
        projectHuawei.setContactPhone("13900139000");
        projectHuawei.setPriceJunior(new BigDecimal("15000"));
        projectHuawei.setPriceMiddle(UNIT_PRICE);
        projectHuawei.setPriceSenior(new BigDecimal("30000"));
        projectMapper.insert(projectHuawei);

        userZhangsan = new User();
        userZhangsan.setUsername("zhangsan_test");
        userZhangsan.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyz");
        userZhangsan.setRealName("张三");
        userZhangsan.setEmail("zhangsan@example.com");
        userZhangsan.setPhone("13800138000");
        userZhangsan.setStatus(1);
        userMapper.insert(userZhangsan);

        assignment = new Assignment();
        assignment.setTalentId(talentZhangsan.getId());
        assignment.setTalentName("张三");
        assignment.setProjectId(projectHuawei.getId());
        assignment.setProjectName("华为云平台项目");
        assignment.setStartDate(LocalDate.of(2026, 6, 10));
        assignment.setEndDate(LocalDate.of(2026, 6, 30));
        assignment.setUnitPrice(UNIT_PRICE);
        assignment.setTalentLevel("MIDDLE");
        assignment.setStatus("ACTIVE");
        assignmentMapper.insert(assignment);

        timesheet = new Timesheet();
        timesheet.setUserId(userZhangsan.getId());
        timesheet.setUserName("张三");
        timesheet.setTalentId(talentZhangsan.getId());
        timesheet.setTalentName("张三");
        timesheet.setProjectId(projectHuawei.getId());
        timesheet.setProjectName("华为云平台项目");
        timesheet.setMonth(TEST_MONTH);
        timesheet.setStatus("APPROVED");
        timesheet.setApproverId(999L);
        timesheet.setApproverName("审批人");
        timesheetMapper.insert(timesheet);

        int[] normalDays = {10, 11, 12, 13, 15, 16, 17, 18, 19, 22, 23, 24, 25, 26, 27};
        for (int day : normalDays) {
            TimesheetDay td = new TimesheetDay();
            td.setTimesheetId(timesheet.getId());
            td.setDayDate(LocalDate.of(2026, 6, day));
            td.setStatus("NORMAL");
            timesheetDayMapper.insert(td);
        }
    }

    @Test
    @DisplayName("结算场景：张三6月10日入职华为项目，出勤15天 - 验证金额按整月均摊（存在的问题）")
    void testSettlement_June10Entry_15Attendance() {
        Settlement settlement = settlementService.generateSettlement(TEST_MONTH, 999L, "财务");

        assertNotNull(settlement, "生成结算单不应为空");
        assertEquals(1, settlement.getItemCount(), "结算单明细应为1条");
        assertNotNull(settlement.getTotalAmount(), "结算总金额不应为空");

        Map<String, Object> detail = settlementService.getSettlementDetail(settlement.getId());
        List<SettlementItem> items = (List<SettlementItem>) detail.get("items");
        assertEquals(1, items.size(), "结算明细数量应为1");

        SettlementItem item = items.get(0);
        assertEquals("张三", item.getTalentName(), "人才姓名应为张三");
        assertEquals("华为云平台项目", item.getProjectName(), "项目名称应为华为云平台项目");
        assertEquals(0, UNIT_PRICE.compareTo(item.getUnitPriceSnapshot()), "单价快照应为21000");
        assertEquals(TOTAL_WORKDAYS, item.getWorkdayCountSnapshot(), "工作日快照应为21");
        assertEquals(ACTUAL_ATTENDANCE, item.getActualAttendanceDays(), "实际出勤应为15天");
        assertEquals(0, item.getOvertimeDays(), "加班天数应为0");

        BigDecimal dailyPriceWholeMonth = UNIT_PRICE.divide(
                BigDecimal.valueOf(TOTAL_WORKDAYS), 4, RoundingMode.HALF_UP);
        BigDecimal expectedWholeMonthAmount = dailyPriceWholeMonth
                .multiply(BigDecimal.valueOf(ACTUAL_ATTENDANCE))
                .setScale(2, RoundingMode.HALF_UP);

        int workdaysFrom10thTo30th = 15;
        BigDecimal dailyPriceProRata = UNIT_PRICE.divide(
                BigDecimal.valueOf(workdaysFrom10thTo30th), 4, RoundingMode.HALF_UP);
        BigDecimal expectedProRataAmount = dailyPriceProRata
                .multiply(BigDecimal.valueOf(ACTUAL_ATTENDANCE))
                .setScale(2, RoundingMode.HALF_UP);

        System.out.println("==== 结算计算过程分析 ====");
        System.out.println("月薪: " + UNIT_PRICE);
        System.out.println("6月总工作日: " + TOTAL_WORKDAYS);
        System.out.println("6月10日-30日工作日数: " + workdaysFrom10thTo30th);
        System.out.println("实际出勤天数: " + ACTUAL_ATTENDANCE);
        System.out.println("--- 按整月均摊(当前代码逻辑) ---");
        System.out.println("日薪 = 21000 ÷ 21 = " + dailyPriceWholeMonth.setScale(2, RoundingMode.HALF_UP));
        System.out.println("结算金额 = " + dailyPriceWholeMonth.setScale(2, RoundingMode.HALF_UP) + " × 15 = " + expectedWholeMonthAmount);
        System.out.println("--- 按入职日到月底折算(正确逻辑) ---");
        System.out.println("日薪 = 21000 ÷ 15 = " + dailyPriceProRata.setScale(2, RoundingMode.HALF_UP));
        System.out.println("结算金额 = " + dailyPriceProRata.setScale(2, RoundingMode.HALF_UP) + " × 15 = " + expectedProRataAmount);
        System.out.println("--- 实际结算结果 ---");
        System.out.println("基础金额 baseAmount: " + item.getBaseAmount());
        System.out.println("最终金额 finalAmount: " + item.getFinalAmount());
        System.out.println("系统计算详情: " + item.getCalcDetail());
        System.out.println("========================");

        assertEquals(0, expectedWholeMonthAmount.compareTo(item.getFinalAmount()),
                "当前代码按整月均摊计算，结果应为 " + expectedWholeMonthAmount +
                        "，而非按入职日折算的 " + expectedProRataAmount);

        assertTrue(expectedWholeMonthAmount.compareTo(expectedProRataAmount) < 0,
                "验证：整月均摊金额(" + expectedWholeMonthAmount +
                        ") 小于 入职折算金额(" + expectedProRataAmount +
                        ")，说明月中入职员工被少算");

        System.out.println("[测试结论] 当前结算引擎按整月21日均摊日薪，未考虑张三6月10日才入职的事实。" +
                "虽然出勤15天（正好是6月10日-30日的全部工作日），" +
                "但仅结算 " + item.getFinalAmount() + " 元，" +
                "比正确折算少 " + expectedProRataAmount.subtract(item.getFinalAmount()) + " 元。");
    }
}
