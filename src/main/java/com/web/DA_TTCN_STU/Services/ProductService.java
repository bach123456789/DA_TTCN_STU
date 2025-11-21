package com.web.DA_TTCN_STU.Services;

import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product không tồn tại với ID: " + id));
    }

    public void update(Product updated) {
        Product existing = productRepository.findById(updated.getProductID())
                .orElseThrow(() -> new RuntimeException("Product không tồn tại với ID: " + updated.getProductID()));

        existing.setProductName(updated.getProductName());
        existing.setPrice(updated.getPrice());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setImageURL(updated.getImageURL());

        productRepository.save(existing);
    }

    public List<Product> findAll() {
        return productRepository.findAll(); // ⚡ Trả về tất cả sản phẩm
    }
}

