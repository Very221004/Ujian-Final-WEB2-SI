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
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
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

    public Page<Product> getProductsByUser(User user, String keyword, Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            Category category = categoryService.findByIdAndOwner(categoryId, user).orElse(null);
            if (category != null) {
                if (keyword != null && !keyword.isEmpty()) {
                    return productRepository.findByOwnerAndNameContainingIgnoreCaseAndCategory(user, keyword, category, pageable);
                }
                return productRepository.findByOwnerAndCategory(user, category, pageable);
            }
        }

        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByOwnerAndNameContainingIgnoreCase(user, keyword, pageable);
        }

        return productRepository.findByOwner(user, pageable);
    }
}