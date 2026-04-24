package com.example.productcrud.controller;

import com.example.productcrud.dto.ChangePasswordRequest;
import com.example.productcrud.dto.ProfileUpdateRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName(user.getFullName());
        request.setEmail(user.getEmail());
        request.setPhoneNumber(user.getPhoneNumber());
        request.setAddress(user.getAddress());
        request.setBio(user.getBio());
        request.setProfileImageUrl(user.getProfileImageUrl());

        model.addAttribute("profileRequest", request);
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute ProfileUpdateRequest request,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);

        // Update profile
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setBio(request.getBio());
        user.setProfileImageUrl(request.getProfileImageUrl());

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Profile berhasil diperbarui!");
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String showChangePassword(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @ModelAttribute ChangePasswordRequest request,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);

        // Validasi: semua field harus diisi
        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password lama tidak boleh kosong!");
            return "redirect:/change-password";
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password baru tidak boleh kosong!");
            return "redirect:/change-password";
        }

        if (request.getConfirmNewPassword() == null || request.getConfirmNewPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi password baru tidak boleh kosong!");
            return "redirect:/change-password";
        }

        // Validasi: password baru dan konfirmasi harus cocok
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password baru dan konfirmasi password tidak cocok!");
            return "redirect:/change-password";
        }

        // Validasi: password baru tidak boleh sama dengan password lama
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password baru tidak boleh sama dengan password lama!");
            return "redirect:/change-password";
        }

        // Validasi: password lama harus benar
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password lama salah!");
            return "redirect:/change-password";
        }

        // Update password dengan BCrypt
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Password berhasil diubah!");
        return "redirect:/profile";
    }
}