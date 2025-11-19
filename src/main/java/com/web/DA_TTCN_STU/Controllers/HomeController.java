package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        try {
            // Gọi service đã có của bạn
            String token = userService.login(email, password);

            // Lưu token + email vào session (KHÔNG COOKIE)
            session.setAttribute("token", token);
            session.setAttribute("email", email);

            // Chuyển đến trang index (không redirect)
            model.addAttribute("email", email);
            model.addAttribute("token", token);

            return "index"; // dùng Thymeleaf → forward, không redirect

        } catch (Exception e) {
            // Báo lỗi
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // xóa token + email
        return "redirect:/login";
    }
}
