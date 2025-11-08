package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}