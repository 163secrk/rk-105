package com.talentbridge.service;

import com.talentbridge.entity.Settlement;
import com.talentbridge.entity.SettlementItem;
import java.util.List;
import java.util.Map;

public interface SettlementService {
    List<Settlement> findAll();
    Settlement findById(Long id);
    List<Settlement> findByMonth(String month);
    Settlement generateSettlement(String month, Long creatorId, String creatorName);
    Map<String, Object> getSettlementDetail(Long id);
    void deleteById(Long id);
}
