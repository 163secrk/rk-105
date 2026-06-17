package com.talentbridge.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Assignment {

    private Long id;

    @NotNull(message = "请选择人才")
    private Long talentId;

    private String talentName;

    @NotNull(message = "请选择项目")
    private Long projectId;

    private String projectName;

    @NotNull(message = "请选择开始日期")
    private LocalDate startDate;

    @NotNull(message = "请选择结束日期")
    private LocalDate endDate;

    @NotNull(message = "请输入结算单价")
    private BigDecimal unitPrice;

    private String talentLevel;

    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
