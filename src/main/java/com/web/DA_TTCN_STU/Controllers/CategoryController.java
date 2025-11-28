package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.DTOs.CategoryDTO;
import com.web.DA_TTCN_STU.Entities.Category;
import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Repositories.CategoryRepository;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/category/list")
    public String listCategories(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String keyword) {

        int pageSize = 5;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Category> categoryPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            categoryPage = categoryRepository.findByCategoryNameContainingIgnoreCase(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        List<CategoryDTO> dtoList = categoryPage.getContent()
                .stream()
                .map(c -> new CategoryDTO(
                        c.getCategoryID(),
                        c.getCategoryName(),
                        c.getProducts().size()
                ))
                .toList();

        model.addAttribute("categories", dtoList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());

        return "category/list";
    }

    @GetMapping("/category/create")
    public String createCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "/category/create";
    }

    @PostMapping("/category/create")
    public String saveCategory(Category category, RedirectAttributes redirect) {
        categoryRepository.save(category);
        redirect.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        return "redirect:/category/list";
    }

    // =======================
    // SHOW EDIT FORM
    // =======================
    @GetMapping("/category/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {

        Optional<Category> opt = categoryRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/category/list";
        }

        model.addAttribute("category", opt.get());
        return "/category/edit";  // file HTML bạn sẽ tạo
    }

    // =======================
    // HANDLE UPDATE CATEGORY
    // =======================
    @PostMapping("/category/edit/{id}")
    public String updateCategory(@PathVariable("id") Long id,
                                 @ModelAttribute Category category,
                                 RedirectAttributes ra) {

        Optional<Category> opt = categoryRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/category/list";
        }

        Category old = opt.get();
        old.setCategoryName(category.getCategoryName());

        categoryRepository.save(old);

        ra.addFlashAttribute("success", "Cập nhật danh mục thành công!");
        return "redirect:/category/list";
    }

    // =======================
    // DELETE CATEGORY
    // =======================
    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes ra) {

        Optional<Category> opt = categoryRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/category/list";
        }

        // Kiểm tra xem có Product thuộc Category này không
        List<Product> products = productRepository.findByCategoryCategoryID(id);

        if (!products.isEmpty()) {
            ra.addFlashAttribute("errorMessage",
                    "Không thể xóa danh mục vì vẫn còn sản phẩm thuộc danh mục này!");
            return "redirect:/category/list";
        }

        // Nếu không có product -> xóa
        categoryRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        return "redirect:/category/list";
    }
}
