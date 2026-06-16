package com.talentbridge.service;

import com.talentbridge.entity.Project;

import java.util.List;

public interface ProjectService {

    List<Project> findAll();

    Project findById(Long id);

    int insert(Project project);

    int update(Project project);

    int deleteById(Long id);
}
