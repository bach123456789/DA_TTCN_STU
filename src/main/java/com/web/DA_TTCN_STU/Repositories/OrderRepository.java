package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Hàm cũ của bạn (Giữ nguyên)
    long countByUserUserID(Long userID);

    // --- MỚI THÊM: Tìm đơn hàng theo UserID, sắp xếp mới nhất lên đầu ---
    List<Order> findByUser_UserIDOrderByOrderDateDesc(Long userID);
}