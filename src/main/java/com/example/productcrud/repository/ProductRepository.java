package com.example.productcrud.repository;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByOwner(User owner, Pageable pageable);

    Page<Product> findByOwnerAndNameContainingIgnoreCase(User owner, String keyword, Pageable pageable);

    Page<Product> findByOwnerAndCategory(User owner, Category category, Pageable pageable);

    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndCategory(User owner, String keyword, Category category, Pageable pageable);

    Optional<Product> findByIdAndOwner(Long id, User owner);
}