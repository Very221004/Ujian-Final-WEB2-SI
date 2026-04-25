package com.example.productcrud.controller;

import com.example.productcrud.dto.CategoryRequest;
import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.CategoryService;
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
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(CategoryService categoryService, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/categories")
    public String listCategories(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        User currentUser = getCurrentUser(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Category> categoryPage = categoryService.getCategoriesByUser(currentUser, keyword, pageable);

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("keyword", keyword);

        return "category/list";
    }

    @GetMapping("/categories/new")
    public String showCreateForm(Model model) {
        model.addAttribute("categoryRequest", new CategoryRequest());
        model.addAttribute("isEdit", false);
        return "category/form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(
            @ModelAttribute CategoryRequest categoryRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long id,
            RedirectAttributes redirectAttributes
    ) {
        User currentUser = getCurrentUser(userDetails);

        // Validasi nama tidak kosong
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nama kategori tidak boleh kosong!");
            return id != null ? "redirect:/categories/" + id + "/edit" : "redirect:/categories/new";
        }

        // Cek nama duplikat
        if (id != null) {
            if (categoryService.isNameExistsForUserExcept(categoryRequest.getName().trim(), currentUser, id)) {
                redirectAttributes.addFlashAttribute("error", "Nama kategori sudah digunakan!");
                return "redirect:/categories/" + id + "/edit";
            }
        } else {
            if (categoryService.isNameExistsForUser(categoryRequest.getName().trim(), currentUser)) {
                redirectAttributes.addFlashAttribute("error", "Nama kategori sudah digunakan!");
                return "redirect:/categories/new";
            }
        }

        Category category;
        if (id != null) {
            category = categoryService.findByIdAndOwner(id, currentUser)
                    .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
            redirectAttributes.addFlashAttribute("success", "Kategori berhasil diperbarui!");
        } else {
            category = new Category();
            category.setOwner(currentUser);
            redirectAttributes.addFlashAttribute("success", "Kategori berhasil ditambahkan!");
        }

        category.setName(categoryRequest.getName().trim());
        category.setDescription(categoryRequest.getDescription() != null ? categoryRequest.getDescription().trim() : null);

        categoryService.save(category);

        return "redirect:/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User currentUser = getCurrentUser(userDetails);

        return categoryService.findByIdAndOwner(id, currentUser)
                .map(category -> {
                    CategoryRequest request = new CategoryRequest();
                    request.setName(category.getName());
                    request.setDescription(category.getDescription());
                    model.addAttribute("categoryRequest", request);
                    model.addAttribute("categoryId", category.getId());
                    model.addAttribute("isEdit", true);
                    return "category/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Kategori tidak ditemukan.");
                    return "redirect:/categories";
                });
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User currentUser = getCurrentUser(userDetails);

        boolean isOwner = categoryService.findByIdAndOwner(id, currentUser).isPresent();

        if (isOwner) {
            categoryService.deleteByIdAndOwner(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Kategori berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Kategori tidak ditemukan.");
        }

        return "redirect:/categories";
    }
}