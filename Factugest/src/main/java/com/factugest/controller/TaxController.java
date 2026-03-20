package com.factugest.controller;

import com.factugest.entity.Impuesto;
import com.factugest.service.ImpuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/invoice_taxes")
public class TaxController {

    @Autowired private ImpuestoService impuestoService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("all_taxes", impuestoService.getAll());
        return "invoice_taxes/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("tax", null);
        return "invoice_taxes/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String descripcion,
            @RequestParam BigDecimal porcentaje,
            @RequestParam(required = false) String codigo_dian) {
        Impuesto i = new Impuesto();
        i.setDescripcion(descripcion);
        i.setPorcentaje(porcentaje);
        i.setCodigoDian(codigo_dian);
        impuestoService.save(i);
        return "redirect:/invoice_taxes";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Impuesto> opt = impuestoService.getById(id);
        if (opt.isEmpty()) return "redirect:/invoice_taxes";
        model.addAttribute("tax", opt.get());
        return "invoice_taxes/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String descripcion,
            @RequestParam BigDecimal porcentaje,
            @RequestParam(required = false) String codigo_dian) {
        Optional<Impuesto> opt = impuestoService.getById(id);
        if (opt.isEmpty()) return "redirect:/invoice_taxes";
        Impuesto i = opt.get();
        i.setDescripcion(descripcion);
        i.setPorcentaje(porcentaje);
        i.setCodigoDian(codigo_dian);
        impuestoService.save(i);
        return "redirect:/invoice_taxes";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        impuestoService.delete(id);
        return "redirect:/invoice_taxes";
    }
}
