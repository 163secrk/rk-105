package com.talentbridge.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardStats {
    private Integer activeTalentCount;
    private Integer idleTalentCount;
    private Integer newProjectCount;
    private BigDecimal monthlyRevenue;
}
