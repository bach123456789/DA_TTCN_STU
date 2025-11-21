package com.web.DA_TTCN_STU.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductDTO {
    private Long productID;
    private String productName;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private String imageURL;
    private String image; //tên file ảnh
    private LocalDateTime createdAt;
}