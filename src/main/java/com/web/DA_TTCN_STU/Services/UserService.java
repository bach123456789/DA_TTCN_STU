package com.web.DA_TTCN_STU.Services;

import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.UserRepository;
import com.web.DA_TTCN_STU.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {

        // 1. kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        // 2. mã hoá mật khẩu
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // 3. lưu user mới
        return userRepository.save(user);
    }
}