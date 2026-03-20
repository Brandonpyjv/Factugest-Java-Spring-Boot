package com.factugest.controller;

import com.factugest.entity.Customer;
import com.factugest.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired private CustomerService customerService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("all_customers", customerService.getAll());
        return "customer/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customer", null);
        return "customer/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String full_name,
            @RequestParam String document_type,
            @RequestParam String document_number,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false, defaultValue = "Colombia") String pais,
            @RequestParam(defaultValue = "NATURAL") String tipo_persona,
            @RequestParam(defaultValue = "NO_RESPONSABLE_IVA") String regimen_tributario) {

        Customer c = new Customer();
        c.setFullName(full_name);
        c.setDocumentType(document_type);
        c.setDocumentNumber(document_number);
        c.setPhone(phone);
        c.setEmail(email);
        c.setAddress(address);
        c.setCiudad(ciudad);
        c.setDepartamento(departamento);
        c.setPais(pais);
        c.setTipoPersona(tipo_persona);
        c.setRegimenTributario(regimen_tributario);
        customerService.save(c);
        return "redirect:/customer";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Customer> opt = customerService.getById(id);
        if (opt.isEmpty()) return "redirect:/customer";
        model.addAttribute("customer", opt.get());
        return "customer/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String full_name,
            @RequestParam String document_type,
            @RequestParam String document_number,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false, defaultValue = "Colombia") String pais,
            @RequestParam(defaultValue = "NATURAL") String tipo_persona,
            @RequestParam(defaultValue = "NO_RESPONSABLE_IVA") String regimen_tributario) {

        Optional<Customer> opt = customerService.getById(id);
        if (opt.isEmpty()) return "redirect:/customer";
        Customer c = opt.get();
        c.setFullName(full_name);
        c.setDocumentType(document_type);
        c.setDocumentNumber(document_number);
        c.setPhone(phone);
        c.setEmail(email);
        c.setAddress(address);
        c.setCiudad(ciudad);
        c.setDepartamento(departamento);
        c.setPais(pais);
        c.setTipoPersona(tipo_persona);
        c.setRegimenTributario(regimen_tributario);
        customerService.save(c);
        return "redirect:/customer";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        customerService.delete(id);
        return "redirect:/customer";
    }
}
