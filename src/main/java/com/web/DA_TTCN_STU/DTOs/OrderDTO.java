package com.web.DA_TTCN_STU.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderID;
    private String customerName;   // hoặc email, tùy bạn
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String status;
}
