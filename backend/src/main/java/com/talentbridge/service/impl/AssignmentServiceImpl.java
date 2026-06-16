package com.talentbridge.service.impl;

import com.talentbridge.entity.Assignment;
import com.talentbridge.mapper.AssignmentMapper;
import com.talentbridge.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentMapper assignmentMapper;

    @Override
    public List<Assignment> findAll() {
        return assignmentMapper.findAll();
    }

    @Override
    public Assignment findById(Long id) {
        return assignmentMapper.findById(id);
    }

    @Override
    public List<Assignment> findActiveByTalentId(Long talentId) {
        return assignmentMapper.findActiveByTalentId(talentId);
    }

    @Override
    public Assignment insert(Assignment assignment) {
        if (assignment.getStatus() == null) {
            assignment.setStatus("ACTIVE");
        }
        assignmentMapper.insert(assignment);
        return assignment;
    }

    @Override
    public int update(Assignment assignment) {
        return assignmentMapper.update(assignment);
    }

    @Override
    public int deleteById(Long id) {
        return assignmentMapper.deleteById(id);
    }

    @Override
    public boolean checkTimeConflict(Long talentId, LocalDate startDate, LocalDate endDate, Long excludeId) {
        List<Assignment> existing;
        if (excludeId != null) {
            existing = assignmentMapper.findActiveByTalentIdExclude(talentId, excludeId);
        } else {
            existing = assignmentMapper.findActiveByTalentId(talentId);
        }

        for (Assignment a : existing) {
            if (isOverlap(startDate, endDate, a.getStartDate(), a.getEndDate())) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}
