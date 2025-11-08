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

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        return jwtUtils.generateToken(email);
    }
}