package com.talentbridge.service;

import com.talentbridge.entity.Talent;

import java.util.List;

public interface TalentService {

    List<Talent> findAll();

    Talent findById(Long id);

    int insert(Talent talent);

    int update(Talent talent);

    int updateStatus(Long id, String status);

    int deleteById(Long id);

    void resign(Long id);
}
