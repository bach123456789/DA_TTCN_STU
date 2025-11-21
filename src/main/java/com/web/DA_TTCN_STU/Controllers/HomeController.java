package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/") //index.html
    public String index(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Product> products;

        if (keyword != null && !keyword.isEmpty()) {
            products = productRepository.findByProductNameContainingIgnoreCase(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            products = productRepository.findAll();
        }

        model.addAttribute("products", products);
        return "index"; // tương ứng với file shop.html
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(value = "cat", required = false) String cat,
                       @RequestParam(value = "price", required = false) String priceRange,
                       @RequestParam(value = "keyword", required = false) String searchKeyword, // <--- 1. THÊM THAM SỐ NÀY
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        int pageSize = 9; // Bạn đang để 9 sản phẩm/trang
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage;

        // --- XỬ LÝ KHOẢNG GIÁ (Giữ nguyên code của bạn) ---
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
        }// --- LOGIC LỌC SẢN PHẨM (Đã thêm phần tìm kiếm) ---

        // TRƯỜNG HỢP 1: Người dùng đang tìm kiếm (Ưu tiên cao nhất)
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            // Gọi hàm tìm theo tên (Nhớ khai báo hàm này trong Repository nhé)
            productPage = productRepository.findByProductNameContainingIgnoreCase(searchKeyword, pageable);
            model.addAttribute("keyword", searchKeyword); // Trả lại từ khóa để hiện trong ô input
        }
        // TRƯỜNG HỢP 2: Người dùng lọc theo danh mục (Category)
        else if (cat != null && !cat.isEmpty()) {
            productPage = productRepository.findByCategory_CategoryNameAndPriceBetween(cat, min, max, pageable);
            model.addAttribute("cat", cat); // Trả lại danh mục để bôi đậm menu
        }
        // TRƯỜNG HỢP 3: Mặc định (Xem tất cả hoặc chỉ lọc theo giá)
        else {
            productPage = productRepository.findByPriceBetween(min, max, pageable);
        }

        // --- TRẢ DỮ LIỆU RA VIEW ---
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "shop";
    }


    @GetMapping("/login")
    public String login() {
        return "login";  // KHÔNG có .html
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            // Gọi service login
            String token = userService.login(email, password);

            // Lưu session
            session.setAttribute("token", token);
            session.setAttribute("email", email);

            // Trả JSON → FE sẽ tự redirect qua index.html
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("token", token);
            response.put("email", email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // xóa token + email
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String register() {
        return "/register";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User newUser = userService.register(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
