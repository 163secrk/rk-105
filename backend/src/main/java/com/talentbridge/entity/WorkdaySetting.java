package com.talentbridge.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkdaySetting {

    private Long id;

    @NotBlank(message = "请选择月份")
    private String month;

    @NotNull(message = "请输入工作日天数")
    @Min(value = 1, message = "工作日天数最小为1")
    @Max(value = 31, message = "工作日天数最大为31")
    private Integer workdayCount;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
