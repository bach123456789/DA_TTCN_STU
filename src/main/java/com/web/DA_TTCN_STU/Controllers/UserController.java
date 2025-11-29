package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.DTOs.OrderDTO;
import com.web.DA_TTCN_STU.Entities.Order;
import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.OrderRepository;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Repositories.UserRepository;
import com.web.DA_TTCN_STU.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
public class UserController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/user/cart")
    public String cart() {
        return "/user/cart";
    }

    @GetMapping("/admin/index")
    public String index(Model model) {
        List<Order> orders = orderRepository.findAll();

        List<OrderDTO> list = orders.stream()
                .map(o -> new OrderDTO(
                        o.getOrderID(),
                        o.getUser().getFullName(),   // hoặc getEmail()
                        o.getTotalAmount(),
                        o.getOrderDate(),
                        o.getStatus()
                ))
                .toList();

        // Tính tổng doanh thu
        BigDecimal totalRevenue = list.stream()
                .map(OrderDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("orders", orders);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("totalRevenue", totalRevenue);
        
        return "/admin/index";
    }

    @GetMapping("/user/list")
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page) {

        int pageSize = 5; // số user mỗi trang (giống giao diện)

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> userPage = userRepository.findAll(pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());

        return "/user/list"; // trỏ đến file HTML
    }

    // ================= CREATE FORM =================
    @GetMapping("/user/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "/user/create";
    }

    // ================= CREATE SUBMIT =================
    @PostMapping("/user/create")
    public String createUser(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String address,
                             @RequestParam String password,
                             @RequestParam String role,
                             Model model) {

        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("errorMessage", "Email đã tồn tại!");
            return "/user/create";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole(role);
        user.setProvider("local"); // user tạo bởi admin là local

        // mã hoá mật khẩu
        user.setPasswordHash(passwordEncoder.encode(password));

        userRepository.save(user);

        return "redirect:/user/list?success=1";
    }

    // GET: Hiển thị form edit
    @GetMapping("/user/edit/{id}")
    public String editUserForm(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        model.addAttribute("user", user);

        return "/user/edit";
    }

    // POST: Lưu update
    @PostMapping("/user/edit/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("user") User updatedUser) {

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        existing.setFullName(updatedUser.getFullName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhone(updatedUser.getPhone());
        existing.setRole(updatedUser.getRole());
        existing.setAddress(updatedUser.getAddress());
        userRepository.save(existing);

        return "redirect:/user/list";
    }

    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id,
                             RedirectAttributes redirect) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirect.addFlashAttribute("error", "Người dùng không tồn tại!");
            return "redirect:/user/list";
        }

        // Kiểm tra user có order nào không
        long orderCount = orderRepository.countByUserUserID(id);
        if (orderCount > 0) {
            redirect.addFlashAttribute("error",
                    "Không thể xoá! Người dùng này có " + orderCount + " đơn hàng.");
            return "redirect:/user/list";
        }

        try {
            userRepository.deleteById(id);
            redirect.addFlashAttribute("success", "Xoá người dùng thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Xoá thất bại! Đã xảy ra lỗi hệ thống.");
        }

        return "redirect:/user/list";
    }
}