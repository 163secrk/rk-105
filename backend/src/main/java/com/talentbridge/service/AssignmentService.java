package com.talentbridge.service;

import com.talentbridge.entity.Assignment;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentService {

    List<Assignment> findAll();

    Assignment findById(Long id);

    List<Assignment> findActiveByTalentId(Long talentId);

    Assignment insert(Assignment assignment);

    int update(Assignment assignment);

    int deleteById(Long id);

    boolean checkTimeConflict(Long talentId, LocalDate startDate, LocalDate endDate, Long excludeId);
}
