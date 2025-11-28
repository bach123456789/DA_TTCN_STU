package com.web.DA_TTCN_STU.Configs;

import com.web.DA_TTCN_STU.Utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            String token = (String) session.getAttribute("token");

            if (token != null && jwtUtils.validateToken(token)) {

                String email = jwtUtils.extractUsername(token);
                String role = jwtUtils.extractRole(token);
                String fullName = jwtUtils.extractFullName(token);
                Long userId = jwtUtils.extractUserId(token);

                // Tự tạo UserDetails dựa trên dữ liệu JWT
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(email)
                        .password("") // không cần
                        .roles(role)
                        .build();


                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
