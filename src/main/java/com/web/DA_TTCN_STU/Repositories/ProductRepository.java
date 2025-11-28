package com.web.DA_TTCN_STU.Repositories;


import com.web.DA_TTCN_STU.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductNameContainingIgnoreCase(String keyword);
    List<Product> findByCategory_CategoryName(String categoryName);
    Page<Product> findByCategory_CategoryName(String categoryName, Pageable pageable);

    Page<Product> findByCategory_CategoryNameAndPriceBetween(
            String categoryName, BigDecimal min, BigDecimal max, Pageable pageable);

    Page<Product> findByPriceBetween(
            BigDecimal min, BigDecimal max, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);
    List<Product> findByCategoryCategoryID(Long categoryID);
}

