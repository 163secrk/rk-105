package com.talentbridge.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementItem {

    private Long id;

    private Long settlementId;

    private Long timesheetId;

    private Long userId;

    private String userName;

    private Long talentId;

    private String talentName;

    private Long projectId;

    private String projectName;

    private BigDecimal unitPriceSnapshot;

    private Integer workdayCountSnapshot;

    private Integer actualAttendanceDays;

    private Integer overtimeDays;

    private BigDecimal overtimeAmount;

    private BigDecimal baseAmount;

    private BigDecimal finalAmount;

    private String calcDetail;

    private LocalDateTime createTime;
}
