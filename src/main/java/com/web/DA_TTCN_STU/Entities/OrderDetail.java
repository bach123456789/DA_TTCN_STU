package com.web.DA_TTCN_STU.Entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderDetailID;

    @ManyToOne
    @JoinColumn(name = "orderID", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "productID", nullable = false)
    private Product product;

    private Integer rating;
    private String comment;
}