package com.example.productcrud.controller;

import com.example.productcrud.service.DashboardService;
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
    public String dashboard(Model model) {

        model.addAttribute("totalProduk", service.totalProduk());
        model.addAttribute("totalValue", service.totalInventoryValue());
        model.addAttribute("aktif", service.totalAktif());
        model.addAttribute("tidakAktif", service.totalTidakAktif());
        model.addAttribute("kategori", service.kategori());
        model.addAttribute("lowStock", service.lowStock());
        model.addAttribute("kategoriLabels", service.kategori().keySet());
        model.addAttribute("kategoriValues", service.kategori().values());

        return "dashboard";
    }
}