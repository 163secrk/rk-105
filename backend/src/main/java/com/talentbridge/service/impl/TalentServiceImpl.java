package com.talentbridge.service.impl;

import com.talentbridge.entity.Talent;
import com.talentbridge.mapper.TalentMapper;
import com.talentbridge.service.TalentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentServiceImpl implements TalentService {

    private final TalentMapper talentMapper;

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
}
