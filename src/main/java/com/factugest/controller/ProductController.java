package com.factugest.controller;

import com.factugest.entity.Producto;
import com.factugest.service.ImpuestoService;
import com.factugest.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/products/product")
public class ProductController {

    @Autowired private ProductoService productoService;
    @Autowired private ImpuestoService impuestoService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productoService.getAllDetailed());
        return "product/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", null);
        model.addAttribute("taxes", impuestoService.getAll());
        return "product/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String sku,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam BigDecimal precio_unitario,
            @RequestParam(defaultValue = "0") Integer stock,
            @RequestParam(defaultValue = "5") Integer stock_minimo,
            @RequestParam Integer cod_impuesto,
            @RequestParam(defaultValue = "C62") String unidad_medida,
            @RequestParam(required = false) String codigo_barras,
            @RequestParam(defaultValue = "1") Integer activo) {

        Producto p = new Producto();
        p.setSku(sku);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecioUnitario(precio_unitario);
        p.setStock(stock);
        p.setStockMinimo(stock_minimo);
        p.setCodImpuesto(cod_impuesto);
        p.setUnidadMedida(unidad_medida);
        p.setCodigoBarras(codigo_barras);
        p.setActivo(activo);
        productoService.save(p);
        return "redirect:/products/product";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Producto> opt = productoService.getById(id);
        if (opt.isEmpty()) return "redirect:/products/product";
        model.addAttribute("product", opt.get());
        model.addAttribute("taxes", impuestoService.getAll());
        return "product/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String sku,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam BigDecimal precio_unitario,
            @RequestParam(defaultValue = "0") Integer stock,
            @RequestParam(defaultValue = "5") Integer stock_minimo,
            @RequestParam Integer cod_impuesto,
            @RequestParam(defaultValue = "C62") String unidad_medida,
            @RequestParam(required = false) String codigo_barras,
            @RequestParam(defaultValue = "1") Integer activo) {

        Optional<Producto> opt = productoService.getById(id);
        if (opt.isEmpty()) return "redirect:/products/product";
        Producto p = opt.get();
        p.setSku(sku);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecioUnitario(precio_unitario);
        p.setStock(stock);
        p.setStockMinimo(stock_minimo);
        p.setCodImpuesto(cod_impuesto);
        p.setUnidadMedida(unidad_medida);
        p.setCodigoBarras(codigo_barras);
        p.setActivo(activo);
        productoService.save(p);
        return "redirect:/products/product";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productoService.delete(id);
        return "redirect:/products/product";
    }
}
