package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.DTOs.ProductDTO;
import com.web.DA_TTCN_STU.Entities.Category;
import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Repositories.CategoryRepository;
import com.web.DA_TTCN_STU.Repositories.OrderDetailRepository;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Hiển thị trang form tạo sản phẩm
    @GetMapping("/product/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product()); // object rỗng để Thymeleaf binding
        model.addAttribute("categories", categoryRepository.findAll());
        return "/product/create"; // product-create.html
    }

    @PostMapping("/product/create")
    public String create(
            @ModelAttribute("product") Product product,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("imageFile") MultipartFile imageFile
    ) throws IOException {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());

        // Lưu file vào thư mục /uploads cùng cấp với src
        String uploadDir = System.getProperty("user.dir") + "/uploads/";

        if (!imageFile.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, imageFile.getBytes());

            // Lưu tên file vào DB
            product.setImageURL("/uploads/" + fileName);  // để load ảnh ra web
            product.setImage(fileName); // cột image (tên file ảnh)
        }

        productRepository.save(product);

        return "redirect:/product/list";
    }

    // Hiển thị form edit
    @GetMapping("/product/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());

        return "/product/edit";
    }

    // Xử lý update
    @PostMapping("/product/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute Product product,
            @RequestParam("categoryID") Long categoryID,
            @RequestParam("imageFile") MultipartFile imageFile
    ) throws IOException {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Cập nhật basic fields
        existing.setProductName(product.getProductName());
        existing.setPrice(product.getPrice());

        // Lấy category từ DB
        Category category = categoryRepository.findById(categoryID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy category"));
        existing.setCategory(category);

        // Nếu chọn ảnh mới
        if (!imageFile.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);

            Files.write(path, imageFile.getBytes());

            existing.setImageURL("/uploads/" + fileName);
            existing.setImage(fileName); // cột image (tên file ảnh)
        }

        productRepository.save(existing);

        return "redirect:/product/list";
    }

    @GetMapping("/product/list")
    public String listProducts(Model model,
                               @RequestParam(defaultValue = "0") int page) {

        int pageSize = 5;

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage = productRepository.findAll(pageable);

        // Map sang DTO để tránh vòng lặp JSON
        List<ProductDTO> dtoList = productPage.getContent().stream()
                .map(p -> new ProductDTO(
                        p.getProductID(),
                        p.getProductName(),
                        p.getCategory().getCategoryName(),
                        p.getPrice(),
                        p.getStock(),
                        p.getImageURL(),
                        p.getImage(),
                        p.getCreatedAt()
                )).toList();

        model.addAttribute("products", dtoList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "product/list";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Kiểm tra product có xuất hiện trong OrderDetail không
        boolean isUsed = orderDetailRepository.existsByProduct_ProductID(id);

        if (isUsed) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Sản phẩm đang có trong đơn hàng. Không thể xoá!"
            );
            return "redirect:/product/list";
        }

        productRepository.delete(product);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Xoá sản phẩm thành công!"
        );

        return "redirect:/product/list";
    }
}
