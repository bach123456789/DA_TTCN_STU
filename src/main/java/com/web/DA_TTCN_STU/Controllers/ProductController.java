package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Repositories.CategoryRepository;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    // Hiển thị trang form tạo sản phẩm
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product()); // object rỗng để Thymeleaf binding
        model.addAttribute("categories", categoryRepository.findAll());
        return "/product/create"; // product-create.html
    }

    // Xử lý submit form
    @PostMapping("/create")
    public String create(@ModelAttribute("product") Product product) {

        // Tự gán thời gian tạo
        product.setCreatedAt(java.time.LocalDateTime.now());

        productRepository.save(product);

        return "/product/list"; // chuyển về danh sách
    }

    @GetMapping("/product/edit/{id}")
    public String update(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);

        model.addAttribute("product", product);
        return "/product/update"; // tên file HTML
    }

    @PostMapping("/product/update")
    public String update(@ModelAttribute Product product) {
        productService.update(product);
        return "/product/list"; // quay lại danh sách
    }
}
