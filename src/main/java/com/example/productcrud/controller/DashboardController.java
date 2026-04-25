package com.example.productcrud.controller;

import com.example.productcrud.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        model.addAttribute("totalProduk", service.totalProduk(userDetails));
        model.addAttribute("totalValue", service.totalInventoryValue(userDetails));
        model.addAttribute("aktif", service.totalAktif(userDetails));
        model.addAttribute("tidakAktif", service.totalTidakAktif(userDetails));
        model.addAttribute("kategori", service.kategori(userDetails));
        model.addAttribute("lowStock", service.lowStock(userDetails));
        model.addAttribute("kategoriLabels", service.kategori(userDetails).keySet());
        model.addAttribute("kategoriValues", service.kategori(userDetails).values());

        return "dashboard";
    }
}