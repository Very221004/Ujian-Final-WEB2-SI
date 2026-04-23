package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllByOwner(User owner) {
        return productRepository.findByOwner(owner, Pageable.unpaged()).getContent();
    }

    public Optional<Product> findByIdAndOwner(Long id, User owner) {
        return productRepository.findByIdAndOwner(id, owner);
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void deleteByIdAndOwner(Long id, User owner) {
        productRepository.findByIdAndOwner(id, owner)
                .ifPresent(productRepository::delete);
    }

    // 🔥 PAGINATION + SEARCH + FILTER
    public Page<Product> getProductsByUser(User user, String keyword, Category category, Pageable pageable) {

        if (keyword != null && !keyword.isEmpty() && category != null) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndCategory(user, keyword, category, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByOwnerAndNameContainingIgnoreCase(user, keyword, pageable);
        } else if (category != null) {
            return productRepository.findByOwnerAndCategory(user, category, pageable);
        } else {
            return productRepository.findByOwner(user, pageable);
        }
    }
}