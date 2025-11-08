package com.web.DA_TTCN_STU.Repositories;

import com.web.DA_TTCN_STU.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}