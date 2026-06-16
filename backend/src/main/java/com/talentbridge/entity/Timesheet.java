package com.talentbridge.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Timesheet {

    private Long id;

    private Long userId;

    private String userName;

    private Long talentId;

    private String talentName;

    private Long projectId;

    private String projectName;

    private String month;

    private String status;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
