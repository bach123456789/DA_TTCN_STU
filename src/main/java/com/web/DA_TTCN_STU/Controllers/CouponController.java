package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.Entities.Coupon;
import com.web.DA_TTCN_STU.Repositories.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;


@Controller
public class CouponController {

    @Autowired
    private CouponRepository couponRepository;

    @GetMapping("/coupon/list")
    public String listCoupons(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "") String keyword) {

        Pageable pageable = PageRequest.of(page, 5);
        Page<Coupon> couponPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            couponPage = couponRepository.findByCodeContaining(keyword, pageable);
        } else {
            couponPage = couponRepository.findAll(pageable);
        }

        model.addAttribute("coupons", couponPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", couponPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "/coupon/list";
    }

    @GetMapping("/coupon/create")
    public String createCouponForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        return "/coupon/create";
    }

    @PostMapping("/coupon/create")
    public String saveCoupon(@ModelAttribute Coupon coupon, RedirectAttributes redirectAttributes) {
        // 1. KIỂM TRA NGÀY NULL
        if (coupon.getStartDate() == null || coupon.getEndDate() == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập ngày bắt đầu và ngày kết thúc.");
            return "redirect:/coupon/create";
        }

        // 2. KIỂM TRA NGÀY KẾT THÚC PHẢI SAU NGÀY BẮT ĐẦU
        if (coupon.getEndDate().isBefore(coupon.getStartDate())) {
            redirectAttributes.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu.");
            return "redirect:/coupon/create";
        }

        // 3. KIỂM TRA ACTIVE DATE RANGE — optional
        if (coupon.getStartDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải từ thời điểm hiện tại trở đi.");
            return "redirect:/coupon/create";
        }

        // 4. LƯU VÀ THÔNG BÁO
        couponRepository.save(coupon);
        redirectAttributes.addFlashAttribute("success", "Tạo coupon thành công!");
        return "redirect:/coupon/list";
    }

    @GetMapping("/coupon/edit/{id}")
    public String editCoupon(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Coupon cp = couponRepository.findById(id).orElse(null);
        if (cp == null) {
            ra.addFlashAttribute("errorMessage", "Coupon không tồn tại!");
            return "redirect:/coupon/list";
        }
        model.addAttribute("coupon", cp);
        return "/coupon/edit";
    }

    @PostMapping("/coupon/edit/{id}")
    public String updateCoupon(
            @PathVariable Long id,
            @ModelAttribute("coupon") Coupon formCoupon,
            RedirectAttributes redirectAttributes
    ) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon không tồn tại"));

        // --- VALIDATE NGÀY ---
        if (formCoupon.getEndDate().isBefore(formCoupon.getStartDate())) {
            redirectAttributes.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu!");
            return "redirect:/coupon/edit/" + id;
        }

        // --- UPDATE DATA ---
        coupon.setCode(formCoupon.getCode());
        coupon.setDescription(formCoupon.getDescription());
        coupon.setDiscountType(formCoupon.getDiscountType());
        coupon.setDiscountValue(formCoupon.getDiscountValue());
        coupon.setStartDate(formCoupon.getStartDate());
        coupon.setEndDate(formCoupon.getEndDate());
        coupon.setActive(formCoupon.getActive());
        coupon.setUsageLimit(formCoupon.getUsageLimit());
        coupon.setUsedCount(formCoupon.getUsedCount());

        couponRepository.save(coupon);

        redirectAttributes.addFlashAttribute("success", "Cập nhật coupon thành công!");
        return "redirect:/coupon/list";
    }

    @GetMapping("/coupon/delete/{id}")
    public String deleteCoupon(@PathVariable Long id, RedirectAttributes ra) {
        if (!couponRepository.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "Coupon không tồn tại!");
            return "redirect:/coupon/list";
        }
        couponRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "Xóa coupon thành công!");
        return "redirect:/coupon/list";
    }
}