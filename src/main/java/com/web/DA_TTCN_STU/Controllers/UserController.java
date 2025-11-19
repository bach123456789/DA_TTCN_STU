package com.web.DA_TTCN_STU.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @GetMapping("/user/cart")
    public String cart() {
        return "/user/cart";
    }

    @GetMapping("/admin/index")
    public String index() {
        return "admin/index";
    }
}