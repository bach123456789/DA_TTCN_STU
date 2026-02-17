package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.Entities.Order;
import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Services.CartService;
import com.web.DA_TTCN_STU.Services.UserService;
import com.web.DA_TTCN_STU.Utils.JwtUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;;

    @Autowired
    private CartService cartService;

    @GetMapping("/")
    public String index(Model model) {
        // 1. Lấy 4 sản phẩm mới nhất để hiện ở trang chủ
        // (PageRequest.of(0, 4) nghĩa là lấy trang đầu tiên, 4 phần tử)
        Page<Product> page = productRepository.findAll(PageRequest.of(0, 4));
        List<Product> newProducts = page.getContent();

        // 2. Gửi danh sách sản phẩm xuống View
        model.addAttribute("products", newProducts);

        // 3. Gửi thông tin giỏ hàng để hiện số lượng trên icon
        model.addAttribute("cartItems", cartService.getItems());

        return "index";
    }


    @GetMapping("/shop")
    public String shop(@RequestParam(value = "cat", required = false) String cat,
                       @RequestParam(value = "price", required = false) String priceRange,
                       @RequestParam(value = "keyword", required = false) String searchKeyword,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        int pageSize = 9;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage;

        // --- XỬ LÝ KHOẢNG GIÁ (Giữ nguyên) ---
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("999999999");

        if (priceRange != null && !priceRange.isEmpty()) {
            if (priceRange.contains("-")) {
                String[] parts = priceRange.split("-");
                min = parts[0].isEmpty() ? BigDecimal.ZERO : new BigDecimal(parts[0]);
                max = (parts.length < 2 || parts[1].isEmpty()) ? new BigDecimal("999999999") : new BigDecimal(parts[1]);
            } else {
                min = BigDecimal.ZERO;
                max = new BigDecimal(priceRange);
            }
            model.addAttribute("price", priceRange);
        }

        // --- LOGIC LỌC SẢN PHẨM (Giữ nguyên) ---
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            productPage = productRepository.findByProductNameContainingIgnoreCase(searchKeyword, pageable);
            model.addAttribute("keyword", searchKeyword);
        }
        else if (cat != null && !cat.isEmpty()) {
            productPage = productRepository.findByCategory_CategoryNameAndPriceBetween(cat, min, max, pageable);
            model.addAttribute("cat", cat);
        }
        else {
            productPage = productRepository.findByPriceBetween(min, max, pageable);
        }

        // --- TRẢ DỮ LIỆU RA VIEW ---
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        // ---> MỚI THÊM: Chỉ thêm đúng dòng này để hiện số trên icon giỏ hàng <---
        model.addAttribute("cartItems", cartService.getItems());

        return "shop";
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // KHÔNG có .html
    }

//    @PostMapping("/login")
//    public String login(@RequestParam("email") String email,
//                        @RequestParam("password") String password,
//                        HttpSession session,
//                        Model model) {
//
//        try {
//            // B1: xác thực bằng AuthenticationManager
//            Authentication auth = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(email, password)
//            );
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            // B2: load user từ DB
//            User user = (User) userDetailsService.loadUserByUsername(email);
//
//            // B3: tạo JWT và lưu vào session
//            String token = jwtUtils.generateToken(user);
//            session.setAttribute("token", token);
//
//            // 👉 lưu user vào session
//            session.setAttribute("user", user);
//
//            // B4: redirect theo quyền
//            if (user.getRole().equals("ADMIN") || user.getRole().equals("STAFF") || user.getRole().equals("MANAGER")) {
//                return "redirect:/admin/index";
//            } else {
//                return "redirect:/";
//            }
//
//        } catch (Exception e) {
//            model.addAttribute("error", "Sai email hoặc mật khẩu");
//            return "/login";
//        }
//    }

    @GetMapping("/register")
    public String register() {
        return "/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String repassword) {
        try {
            if(!password.equals(repassword)) {
                throw new Exception("Mật khẩu và nhập lại mật khẩu không trùng");
            }
            User user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPasswordHash(password);
            userService.register(user);
            return "redirect:/login";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
