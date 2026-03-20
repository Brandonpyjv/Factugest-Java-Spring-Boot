package com.factugest.controller;

import com.factugest.entity.MetodoPago;
import com.factugest.service.MetodoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/payment_methods")
public class PaymentMethodController {

    @Autowired private MetodoPagoService metodoPagoService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("all_payment_methods", metodoPagoService.getAll());
        return "payment_methods/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("pm", null);
        return "payment_methods/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String descripcion,
            @RequestParam(required = false) String nombre) {
        MetodoPago m = new MetodoPago();
        m.setDescripcion(descripcion);
        m.setNombre(nombre);
        metodoPagoService.save(m);
        return "redirect:/payment_methods";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<MetodoPago> opt = metodoPagoService.getById(id);
        if (opt.isEmpty()) return "redirect:/payment_methods";
        model.addAttribute("pm", opt.get());
        return "payment_methods/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String descripcion,
            @RequestParam(required = false) String nombre) {
        Optional<MetodoPago> opt = metodoPagoService.getById(id);
        if (opt.isEmpty()) return "redirect:/payment_methods";
        MetodoPago m = opt.get();
        m.setDescripcion(descripcion);
        m.setNombre(nombre);
        metodoPagoService.save(m);
        return "redirect:/payment_methods";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        metodoPagoService.delete(id);
        return "redirect:/payment_methods";
    }
}
