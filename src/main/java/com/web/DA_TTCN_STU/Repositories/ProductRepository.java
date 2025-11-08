package com.web.DA_TTCN_STU.Repositories;


import com.web.DA_TTCN_STU.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_CategoryID(Long categoryId);
    List<Product> findByProductNameContainingIgnoreCase(String keyword);
}