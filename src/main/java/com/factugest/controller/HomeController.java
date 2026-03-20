package com.factugest.controller;

import com.factugest.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @Autowired private InvoiceService invoiceService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("stats", invoiceService.getDashboardStats());
        return "index";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings/index";
    }
}
