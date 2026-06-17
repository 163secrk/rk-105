package com.talentbridge.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonthlyProfit {
    private String month;
    private BigDecimal totalSettlement;
    private BigDecimal totalSalaryCost;
    private BigDecimal profit;
}
