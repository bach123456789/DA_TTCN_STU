package com.web.DA_TTCN_STU.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    private String discountType; // PERCENT | AMOUNT

    private BigDecimal discountValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean active = true;
    private Integer usageLimit = 1;
    private Integer usedCount = 0;
}