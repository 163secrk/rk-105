package com.talentbridge.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProjectProfit {
    private Long projectId;
    private String projectName;
    private BigDecimal totalSettlement;
    private BigDecimal totalSalaryCost;
    private BigDecimal profit;
}
