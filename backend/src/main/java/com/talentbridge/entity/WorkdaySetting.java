package com.talentbridge.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkdaySetting {

    private Long id;

    private String month;

    private Integer workdayCount;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
