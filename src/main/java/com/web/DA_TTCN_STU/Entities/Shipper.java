package com.web.DA_TTCN_STU.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Shipper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipperID;

    private String shipperName;
    private String phone;

    @OneToMany(mappedBy = "shipper", cascade = CascadeType.ALL)
    private List<Order> orders;
}