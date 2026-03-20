package com.factugest.controller;

import com.factugest.entity.PagoFactura;
import com.factugest.service.PagoFacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/invoice_payments")
public class InvoicePaymentController {

    @Autowired private PagoFacturaService pagoFacturaService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("all_invoices", pagoFacturaService.getAll());
        return "invoice_payments/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("payment", null);
        return "invoice_payments/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String status) {
        PagoFactura p = new PagoFactura();
        p.setStatus(status);
        pagoFacturaService.save(p);
        return "redirect:/invoice_payments";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<PagoFactura> opt = pagoFacturaService.getById(id);
        if (opt.isEmpty()) return "redirect:/invoice_payments";
        model.addAttribute("payment", opt.get());
        return "invoice_payments/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id, @RequestParam String status) {
        Optional<PagoFactura> opt = pagoFacturaService.getById(id);
        if (opt.isEmpty()) return "redirect:/invoice_payments";
        PagoFactura p = opt.get();
        p.setStatus(status);
        pagoFacturaService.save(p);
        return "redirect:/invoice_payments";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        pagoFacturaService.delete(id);
        return "redirect:/invoice_payments";
    }
}
