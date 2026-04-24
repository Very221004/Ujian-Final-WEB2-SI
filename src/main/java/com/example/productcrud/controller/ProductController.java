package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    // Ambil user login
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/products";
    }

    // ================================
    // LIST + PAGINATION + SEARCH + FILTER
    // ================================
    @GetMapping("/products")
    public String listProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            Model model
    ) {

        User currentUser = getCurrentUser(userDetails);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage = productService
                .getProductsByUser(currentUser, keyword, category, pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);

        return "product/list";
    }

    // ================================
    // DETAIL
    // ================================
    @GetMapping("/products/{id}")
    public String detailProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
                    return "redirect:/products";
                });
    }

    // ================================
    // FORM CREATE
    // ================================
    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        Product product = new Product();
        product.setCreatedAt(LocalDate.now());

        model.addAttribute("product", product);
        model.addAttribute("categories", Category.values());

        return "product/form";
    }

    // ================================
    // SAVE (CREATE + UPDATE)
    // ================================
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        try {

            // VALIDASI SEDERHANA
            if (product.getName() == null || product.getName().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nama produk tidak boleh kosong!");
                return "redirect:/products/new";
            }

            // CEK OWNER (EDIT)
            if (product.getId() != null) {
                boolean isOwner = productService
                        .findByIdAndOwner(product.getId(), currentUser)
                        .isPresent();

                if (!isOwner) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
                    return "redirect:/products";
                }
            }

            // WAJIB SET OWNER
            product.setOwner(currentUser);

            // SET TANGGAL JIKA NULL
            if (product.getCreatedAt() == null) {
                product.setCreatedAt(LocalDate.now());
            }

            productService.save(product);

            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil disimpan!");
            return "redirect:/products";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menyimpan: " + e.getMessage());
            return "redirect:/products/new";
        }
    }

    // ================================
    // EDIT
    // ================================
    @GetMapping("/products/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("categories", Category.values());
                    return "product/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
                    return "redirect:/products";
                });
    }

    // ================================
    // DELETE
    // ================================
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        boolean isOwner = productService
                .findByIdAndOwner(id, currentUser)
                .isPresent();

        if (isOwner) {
            productService.deleteByIdAndOwner(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
        }

        return "redirect:/products";
    }
}
