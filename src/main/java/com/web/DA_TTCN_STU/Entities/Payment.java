package com.web.DA_TTCN_STU.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentID;

    @OneToOne
    @JoinColumn(name = "orderID")
    private Order order;

    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paidAt;
}