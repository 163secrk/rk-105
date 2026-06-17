package com.talentbridge.service.impl;

import com.talentbridge.entity.Talent;
import com.talentbridge.entity.Timesheet;
import com.talentbridge.mapper.TalentMapper;
import com.talentbridge.mapper.TimesheetMapper;
import com.talentbridge.service.TalentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentServiceImpl implements TalentService {

    private final TalentMapper talentMapper;
    private final TimesheetMapper timesheetMapper;

    @Override
    public List<Talent> findAll() {
        return talentMapper.findAll();
    }

    @Override
    public Talent findById(Long id) {
        return talentMapper.findById(id);
    }

    @Override
    public int insert(Talent talent) {
        return talentMapper.insert(talent);
    }

    @Override
    public int update(Talent talent) {
        return talentMapper.update(talent);
    }

    @Override
    public int updateStatus(Long id, String status) {
        return talentMapper.updateStatus(id, status);
    }

    @Override
    public int deleteById(Long id) {
        return talentMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void resign(Long id) {
        Talent talent = talentMapper.findById(id);
        if (talent == null) {
            throw new RuntimeException("人才不存在");
        }
        List<Timesheet> pendingTimesheets = timesheetMapper.findSubmittedByTalentId(id);
        if (pendingTimesheets != null && !pendingTimesheets.isEmpty()) {
            throw new RuntimeException("该人才存在待审批的工时单，无法办理离职，请先处理工时单");
        }
        talentMapper.updateStatus(id, "RESIGNED");
    }
}
