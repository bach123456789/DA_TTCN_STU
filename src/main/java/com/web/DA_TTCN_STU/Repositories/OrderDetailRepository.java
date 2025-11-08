package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrder_OrderID(Long orderId);
}