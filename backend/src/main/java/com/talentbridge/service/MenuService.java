package com.talentbridge.service;

import com.talentbridge.entity.Menu;

import java.util.List;

public interface MenuService {

    List<Menu> findAll();

    List<Menu> findMenusByUserId(Long userId);
}
