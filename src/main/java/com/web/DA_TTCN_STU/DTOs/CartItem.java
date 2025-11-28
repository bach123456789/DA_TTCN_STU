package com.web.DA_TTCN_STU.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String imageURL;
    private int quantity;

    // Tính tổng tiền = giá * số lượng
    public BigDecimal getTotalPrice() {
        if (price == null) return BigDecimal.ZERO;
        return this.price.multiply(BigDecimal.valueOf(this.quantity));
    }
}