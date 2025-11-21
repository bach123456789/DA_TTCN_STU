package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String shop(@RequestParam(value = "", required = false) String cat, Model model) {
        List<Product> products;

        if (cat != null && !cat.isEmpty()) {
            products = productRepository.findByCategory_CategoryName(cat);
            model.addAttribute("keyword", cat);
        } else {
            products = productRepository.findAll();
        }

        model.addAttribute("products", products);
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
