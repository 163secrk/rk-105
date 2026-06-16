package com.talentbridge.service;

import com.talentbridge.entity.User;

public interface UserService {

    User findByUsername(String username);

    User findById(Long id);

    User getUserWithRoles(Long id);
}
