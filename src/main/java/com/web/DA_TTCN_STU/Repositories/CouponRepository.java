package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.Coupon;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByCode(String code);
    Page<Coupon> findByCodeContaining(String code, Pageable pageable);
    Coupon findByCode(String code);
}