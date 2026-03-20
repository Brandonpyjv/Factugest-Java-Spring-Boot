package com.factugest.controller;

import com.factugest.entity.Empresa;
import com.factugest.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/branches")
public class BranchController {

    @Autowired private EmpresaService empresaService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", empresaService.getAll());
        return "branches/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("branch", null);
        return "branches/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String nombre,
            @RequestParam String nit,
            @RequestParam(required = false) String dv,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String correo,
            @RequestParam(defaultValue = "RESPONSABLE_IVA") String regimen_tributario,
            @RequestParam(required = false) String actividad_economica,
            @RequestParam(defaultValue = "NIT") String tipo_documento) {

        Empresa e = new Empresa();
        e.setNombre(nombre);
        e.setNit(nit);
        e.setDv(dv);
        e.setDireccion(direccion);
        e.setCiudad(ciudad);
        e.setTelefono(telefono);
        e.setCorreo(correo);
        e.setRegimenTributario(regimen_tributario);
        e.setActividadEconomica(actividad_economica);
        e.setTipoDocumento(tipo_documento);
        empresaService.save(e);
        return "redirect:/branches";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Empresa> opt = empresaService.getById(id);
        if (opt.isEmpty()) return "redirect:/branches";
        model.addAttribute("branch", opt.get());
        return "branches/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String nombre,
            @RequestParam String nit,
            @RequestParam(required = false) String dv,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String correo,
            @RequestParam(defaultValue = "RESPONSABLE_IVA") String regimen_tributario,
            @RequestParam(required = false) String actividad_economica,
            @RequestParam(defaultValue = "NIT") String tipo_documento) {

        Optional<Empresa> opt = empresaService.getById(id);
        if (opt.isEmpty()) return "redirect:/branches";
        Empresa e = opt.get();
        e.setNombre(nombre);
        e.setNit(nit);
        e.setDv(dv);
        e.setDireccion(direccion);
        e.setCiudad(ciudad);
        e.setTelefono(telefono);
        e.setCorreo(correo);
        e.setRegimenTributario(regimen_tributario);
        e.setActividadEconomica(actividad_economica);
        e.setTipoDocumento(tipo_documento);
        empresaService.save(e);
        return "redirect:/branches";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        empresaService.delete(id);
        return "redirect:/branches";
    }
}
