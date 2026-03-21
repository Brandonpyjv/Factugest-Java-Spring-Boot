package com.factugest.controller;

import com.factugest.entity.Customer;
import com.factugest.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * CRUD de clientes.
 *
 * El mismo formulario (customer/form.html) sirve tanto para crear como para editar.
 * El truco: en el GET de /new ponemos customer=null en el modelo, y en el GET de
 * /edit/{id} ponemos el objeto real. Thymeleaf usa th:field con el objeto y
 * rellena los campos automáticamente cuando existe, o los deja vacíos cuando es null.
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired private CustomerService customerService;

    /** Lista todos los clientes ordenados alfabéticamente. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("all_customers", customerService.getAll());
        return "customer/index";
    }

    /** Formulario en blanco para crear un cliente nuevo. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customer", null);
        return "customer/form";
    }

    /**
     * Persiste un nuevo cliente con los datos del formulario.
     * Los campos opcionales (@RequestParam required=false) pueden venir vacíos
     * desde el HTML sin causar error 400 Bad Request.
     */
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

        // Patrón POST-Redirect-GET: redirige al listado para evitar doble envío si el
        // usuario recarga la página (el navegador pide un GET, no reenvía el POST).
        return "redirect:/customer";
    }

    /**
     * Formulario precargado con los datos del cliente a editar.
     * Optional.isEmpty() protege contra IDs inventados en la URL.
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Customer> opt = customerService.getById(id);
        if (opt.isEmpty()) return "redirect:/customer";
        model.addAttribute("customer", opt.get());
        return "customer/form";
    }

    /** Actualiza los datos de un cliente existente. Mismo patrón que create. */
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

    /** Elimina el cliente. En producción convendría verificar que no tenga facturas asociadas. */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        customerService.delete(id);
        return "redirect:/customer";
    }
}
