package com.factugest.controller;

import com.factugest.entity.Descuento;
import com.factugest.service.DescuentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/discount")
public class DiscountController {

    @Autowired private DescuentoService descuentoService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("all_discount", descuentoService.getAll());
        return "discount/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("discount", null);
        return "discount/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String descripcion,
            @RequestParam BigDecimal porcentaje,
            @RequestParam(required = false) Integer aplica_a_producto,
            @RequestParam(required = false) Integer aplica_a_factura) {
        Descuento d = new Descuento();
        d.setDescripcion(descripcion);
        d.setPorcentaje(porcentaje);
        d.setAplicaAProducto(aplica_a_producto != null ? 1 : 0);
        d.setAplicaAFactura(aplica_a_factura != null ? 1 : 0);
        descuentoService.save(d);
        return "redirect:/discount";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Descuento> opt = descuentoService.getById(id);
        if (opt.isEmpty()) return "redirect:/discount";
        model.addAttribute("discount", opt.get());
        return "discount/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String descripcion,
            @RequestParam BigDecimal porcentaje,
            @RequestParam(required = false) Integer aplica_a_producto,
            @RequestParam(required = false) Integer aplica_a_factura) {
        Optional<Descuento> opt = descuentoService.getById(id);
        if (opt.isEmpty()) return "redirect:/discount";
        Descuento d = opt.get();
        d.setDescripcion(descripcion);
        d.setPorcentaje(porcentaje);
        d.setAplicaAProducto(aplica_a_producto != null ? 1 : 0);
        d.setAplicaAFactura(aplica_a_factura != null ? 1 : 0);
        descuentoService.save(d);
        return "redirect:/discount";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        descuentoService.delete(id);
        return "redirect:/discount";
    }
}
