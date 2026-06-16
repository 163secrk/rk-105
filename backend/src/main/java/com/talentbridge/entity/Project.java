package com.talentbridge.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Project {

    private Long id;

    private String projectName;

    private String clientName;

    private String contactPerson;

    private String contactPhone;

    private BigDecimal priceJunior;

    private BigDecimal priceMiddle;

    private BigDecimal priceSenior;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
