package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.DTOs.OrderDTO;
import com.web.DA_TTCN_STU.Entities.Order;
import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.OrderRepository;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Repositories.UserRepository;
import com.web.DA_TTCN_STU.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
}