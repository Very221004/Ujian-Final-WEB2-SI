package com.example.productcrud.repository;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwner(User owner);

    Page<Category> findByOwner(User owner, Pageable pageable);

    Page<Category> findByOwnerAndNameContainingIgnoreCase(User owner, String keyword, Pageable pageable);

    Optional<Category> findByIdAndOwner(Long id, User owner);

    boolean existsByNameAndOwner(String name, User owner);

    boolean existsByNameAndOwnerAndIdNot(String name, User owner, Long id);
}