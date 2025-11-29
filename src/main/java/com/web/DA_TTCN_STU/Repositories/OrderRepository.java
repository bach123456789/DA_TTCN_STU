package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByUserUserID(Long userID);
}