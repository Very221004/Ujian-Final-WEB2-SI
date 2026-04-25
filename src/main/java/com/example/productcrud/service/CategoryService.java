package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAllByOwner(User owner) {
        return categoryRepository.findByOwner(owner);
    }

    public Page<Category> getCategoriesByUser(User user, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return categoryRepository.findByOwnerAndNameContainingIgnoreCase(user, keyword, pageable);
        }
        return categoryRepository.findByOwner(user, pageable);
    }

    public Optional<Category> findByIdAndOwner(Long id, User owner) {
        return categoryRepository.findByIdAndOwner(id, owner);
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public void deleteByIdAndOwner(Long id, User owner) {
        categoryRepository.findByIdAndOwner(id, owner)
                .ifPresent(categoryRepository::delete);
    }

    public boolean isNameExistsForUser(String name, User owner) {
        return categoryRepository.existsByNameAndOwner(name, owner);
    }

    public boolean isNameExistsForUserExcept(String name, User owner, Long id) {
        return categoryRepository.existsByNameAndOwnerAndIdNot(name, owner, id);
    }
}