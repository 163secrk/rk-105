package com.talentbridge.service.impl;

import com.talentbridge.entity.Menu;
import com.talentbridge.mapper.MenuMapper;
import com.talentbridge.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> findAll() {
        return menuMapper.findAll();
    }

    @Override
    public List<Menu> findMenusByUserId(Long userId) {
        return menuMapper.findMenusByUserId(userId);
    }
}
