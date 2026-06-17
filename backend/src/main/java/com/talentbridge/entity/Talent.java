package com.talentbridge.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Talent {

    private Long id;

    @NotBlank(message = "请输入姓名")
    private String name;

    private String gender;

    @NotBlank(message = "请输入手机号码")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码（11位，以1开头）")
    private String phone;

    @NotBlank(message = "请输入邮箱")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "请输入正确的邮箱地址")
    private String email;

    @NotBlank(message = "请选择职级")
    private String level;

    private BigDecimal monthlySalary;

    private String techStack;

    @NotBlank(message = "请选择状态")
    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
