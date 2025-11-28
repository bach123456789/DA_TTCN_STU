package com.web.DA_TTCN_STU.Services;

import com.web.DA_TTCN_STU.DTOs.CartItem;
import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope // QUAN TRỌNG: Mỗi khách 1 giỏ hàng riêng
public class CartService {

    @Autowired
    private ProductRepository productRepository;

    private List<CartItem> items = new ArrayList<>();

    // 1. Thêm vào giỏ
    public void addToCart(Long productId, int quantity) {
        // Kiểm tra trùng
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Nếu chưa có, lấy từ DB
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            // Lưu ý: Product Entity của bạn phải có field 'price' là Double hoặc BigDecimal.
            // Nếu Product là Double, cần chuyển sang BigDecimal: BigDecimal.valueOf(product.getPrice())
            CartItem newItem = new CartItem(
                    product.getProductID(),
                    product.getProductName(),
                    product.getPrice(), // <--- SỬA: Truyền trực tiếp luôn
                    product.getImageURL(),
                    quantity
            );
            items.add(newItem);
        }
    }

    // 2. Lấy danh sách
    public List<CartItem> getItems() {
        return items;
    }

    // 3. Xóa
    public void remove(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    // 4. Cập nhật số lượng
    public void updateQuantity(Long productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    // 5. Tính tổng tiền
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}