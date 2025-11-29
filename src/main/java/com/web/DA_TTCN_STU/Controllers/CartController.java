package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Nhớ import cái này để hiện thông báo

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 1. XEM GIỎ HÀNG (ĐÃ SỬA: Gửi đủ biến xuống View)
    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getItems());

        // --- CÁC BIẾN MỚI CHO COUPON ---
        model.addAttribute("subTotal", cartService.getSubTotal());         // Tạm tính
        model.addAttribute("discountAmount", cartService.getDiscountAmount()); // Số tiền giảm
        model.addAttribute("totalAmount", cartService.getTotalAmount());   // Tổng cuối cùng
        model.addAttribute("appliedCoupon", cartService.getAppliedCoupon()); // Mã đang dùng

        return "/user/cart";
    }

    // 2. Thêm vào giỏ
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity) {
        cartService.addToCart(productId, quantity);
        return "redirect:/cart";
    }

    // 3. Xóa sản phẩm
    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Long id) {
        cartService.remove(id);
        return "redirect:/cart";
    }

    // 4. Cập nhật số lượng
    @PostMapping("/update")
    public String update(@RequestParam("productId") Long productId,
                         @RequestParam("quantity") int quantity) {
        cartService.updateQuantity(productId, quantity);
        return "redirect:/cart";
    }

    // 5. MỚI THÊM: Xử lý áp mã giảm giá
    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam("code") String code, RedirectAttributes redirectAttributes) {
        String message = cartService.applyCoupon(code);

        if (message.equals("Áp dụng mã thành công!")) {
            redirectAttributes.addFlashAttribute("success", message); // Hiện màu xanh
        } else {
            redirectAttributes.addFlashAttribute("error", message);   // Hiện màu đỏ
        }

        return "redirect:/cart";
    }

    // 6. MỚI THÊM: Xóa mã giảm giá
    @GetMapping("/clear-coupon")
    public String clearCoupon() {
        cartService.removeCoupon();
        return "redirect:/cart";
    }
}