package com.talentbridge.service.impl;

import com.talentbridge.entity.User;
import com.talentbridge.mapper.UserMapper;
import com.talentbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public User getUserWithRoles(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setRoles(userMapper.findRolesByUserId(id));
        }
        return user;
    }
}
