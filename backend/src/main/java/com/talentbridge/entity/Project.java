package com.talentbridge.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Project {

    private Long id;

    @NotBlank(message = "请输入项目名称")
    private String projectName;

    private String clientName;

    private String contactPerson;

    @NotBlank(message = "请输入联系电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码（11位，以1开头）")
    private String contactPhone;

    private BigDecimal priceJunior;

    private BigDecimal priceMiddle;

    private BigDecimal priceSenior;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
