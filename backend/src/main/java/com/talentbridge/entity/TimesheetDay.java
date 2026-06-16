package com.talentbridge.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TimesheetDay {

    private Long id;

    private Long timesheetId;

    private LocalDate dayDate;

    private String status;
}
