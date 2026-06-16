package com.talentbridge.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Talent {

    private Long id;

    private String name;

    private String gender;

    private String phone;

    private String email;

    private String level;

    private BigDecimal monthlySalary;

    private String techStack;

    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
