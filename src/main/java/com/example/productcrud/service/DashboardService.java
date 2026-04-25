package com.example.productcrud.service;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProductRepository repo;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public DashboardService(ProductRepository repo, CategoryService categoryService, UserRepository userRepository) {
        this.repo = repo;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    public long totalProduk(UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return repo.findByOwner(user, Pageable.unpaged()).getTotalElements();
    }

    public double totalInventoryValue(UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return repo.findByOwner(user, Pageable.unpaged()).getContent()
                .stream()
                .mapToDouble(p -> p.getPrice() * p.getStock())
                .sum();
    }

    public long totalAktif(UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return repo.findByOwner(user, Pageable.unpaged()).getContent()
                .stream()
                .filter(Product::isActive)
                .count();
    }

    public long totalTidakAktif(UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return repo.findByOwner(user, Pageable.unpaged()).getContent()
                .stream()
                .filter(p -> !p.isActive())
                .count();
    }

    public Map<String, Long> kategori(UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return repo.findByOwner(user, Pageable.unpaged()).getContent()
                .stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.counting()
                ));
    }

    public List<Product> lowStock(UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return repo.findByOwner(user, Pageable.unpaged()).getContent()
                .stream()
                .filter(p -> p.getStock() < 5)
                .toList();
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }
}