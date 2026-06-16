package com.talentbridge.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Settlement {

    private Long id;

    private String settlementNo;

    private String month;

    private Integer workdayCount;

    private BigDecimal totalAmount;

    private Integer itemCount;

    private String status;

    private Long creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
