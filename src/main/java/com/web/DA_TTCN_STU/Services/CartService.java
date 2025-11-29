package com.web.DA_TTCN_STU.Services;

import com.web.DA_TTCN_STU.DTOs.CartItem;
import com.web.DA_TTCN_STU.Entities.Coupon; // MỚI THÊM
import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Repositories.CouponRepository; // MỚI THÊM
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {

    @Autowired
    private ProductRepository productRepository;

    // --- 1. MỚI THÊM: Inject CouponRepository để tìm mã ---
    @Autowired
    private CouponRepository couponRepository;

    private List<CartItem> items = new ArrayList<>();

    // --- 2. MỚI THÊM: Biến lưu mã giảm giá đang dùng ---
    private Coupon appliedCoupon = null;

    // -----------------------------------------------------------
    // CÁC HÀM CŨ (GIỮ NGUYÊN KHÔNG ĐỔI)
    // -----------------------------------------------------------

    public void addToCart(Long productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            CartItem newItem = new CartItem(
                    product.getProductID(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getImageURL(),
                    quantity
            );
            items.add(newItem);
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void remove(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void updateQuantity(Long productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    // -----------------------------------------------------------
    // CÁC HÀM MỚI (XỬ LÝ TIỀN VÀ COUPON)
    // -----------------------------------------------------------

    // 3. MỚI THÊM: Hàm xử lý áp dụng mã
    public String applyCoupon(String code) {
        // Tìm mã trong DB (Nhớ thêm hàm findByCode vào CouponRepository nhé)
        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) return "Mã giảm giá không tồn tại!";

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now)) return "Mã chưa đến đợt sử dụng!";
        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(now)) return "Mã đã hết hạn!";
        if (coupon.getUsageLimit() > 0 && coupon.getUsedCount() >= coupon.getUsageLimit()) return "Mã đã hết lượt dùng!";

        this.appliedCoupon = coupon;
        return "Áp dụng mã thành công!";
    }

    // 4. MỚI THÊM: Hủy mã
    public void removeCoupon() {
        this.appliedCoupon = null;
    }

    // 5. MỚI THÊM: Lấy mã đang dùng (để Controller gửi ra View)
    public Coupon getAppliedCoupon() {
        return this.appliedCoupon;
    }

    // 6. SỬA ĐỔI: Hàm getTotalAmount cũ bây giờ đổi tên thành getSubTotal (Tạm tính)
    public BigDecimal getSubTotal() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 7. Tính số tiền được giảm
    public BigDecimal getDiscountAmount() {
        if (appliedCoupon == null) return BigDecimal.ZERO;

        BigDecimal subTotal = getSubTotal();

        // Giả sử discountType lưu String "PERCENT" hoặc số 1, 2... tùy bạn quy định
        // Ở đây check theo chuỗi
        if ("PERCENT".equalsIgnoreCase(String.valueOf(appliedCoupon.getDiscountType()))) {
            // Công thức: Tạm tính * (Giá trị / 100)
            // SỬA: Bỏ BigDecimal.valueOf() ở discountValue
            return subTotal.multiply(appliedCoupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        } else {
            // Giảm thẳng tiền mặt
            // SỬA: Bỏ BigDecimal.valueOf(), trả về trực tiếp
            return appliedCoupon.getDiscountValue();
        }
    }

    // 8. MỚI THÊM: Hàm getTotalAmount (TỔNG CUỐI CÙNG)
    // Công thức: Tạm tính - Giảm giá
    public BigDecimal getTotalAmount() {
        BigDecimal subTotal = getSubTotal();
        BigDecimal discount = getDiscountAmount();

        BigDecimal finalPrice = subTotal.subtract(discount);

        // Nếu trừ ra âm thì trả về 0
        return finalPrice.compareTo(BigDecimal.ZERO) > 0 ? finalPrice : BigDecimal.ZERO;
    }

    // 9. MỚI THÊM: Xóa sạch giỏ hàng (Dùng khi thanh toán xong)
    public void clear() {
        items.clear();
        appliedCoupon = null;
    }
}