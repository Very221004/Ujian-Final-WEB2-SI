package com.example.productcrud.service;

import com.example.productcrud.model.Product;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProductRepository repo;

    public DashboardService(ProductRepository repo) {
        this.repo = repo;
    }

    public long totalProduk() {
        return repo.count();
    }

    public double totalInventoryValue() {
        return repo.findAll()
                .stream()
                .mapToDouble(p -> p.getPrice() * p.getStock())
                .sum();
    }

    public long totalAktif() {
        return repo.findAll().stream().filter(Product::isActive).count();
    }

    public long totalTidakAktif() {
        return repo.findAll().stream().filter(p -> !p.isActive()).count();
    }

    public Map<String, Long> kategori() {
        return repo.findAll().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getDisplayName(),
                        Collectors.counting()
                ));
    }

    public List<Product> lowStock() {
        return repo.findAll().stream()
                .filter(p -> p.getStock() < 5)
                .toList();
    }
}