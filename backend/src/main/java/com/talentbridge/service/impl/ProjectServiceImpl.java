package com.talentbridge.service.impl;

import com.talentbridge.entity.Project;
import com.talentbridge.mapper.ProjectMapper;
import com.talentbridge.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    @Override
    public List<Project> findAll() {
        return projectMapper.findAll();
    }

    @Override
    public Project findById(Long id) {
        return projectMapper.findById(id);
    }

    @Override
    public int insert(Project project) {
        return projectMapper.insert(project);
    }

    @Override
    public int update(Project project) {
        return projectMapper.update(project);
    }

    @Override
    public int deleteById(Long id) {
        return projectMapper.deleteById(id);
    }
}
