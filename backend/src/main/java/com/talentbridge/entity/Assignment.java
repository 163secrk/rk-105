package com.talentbridge.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Assignment {

    private Long id;

    private Long talentId;

    private String talentName;

    private Long projectId;

    private String projectName;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal unitPrice;

    private String talentLevel;

    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
