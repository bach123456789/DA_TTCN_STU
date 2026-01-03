package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Tìm chi tiết theo ID đơn hàng (Dùng để xem chi tiết đơn)
    List<OrderDetail> findByOrder_OrderID(Long orderID);

    // Kiểm tra sản phẩm đã từng được mua chưa (Dùng khi xóa sản phẩm)
    boolean existsByProduct_ProductID(Long productID);
}