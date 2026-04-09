package com.factugest.controller;

import com.factugest.entity.Usuario;
import com.factugest.service.EmpresaService;
import com.factugest.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * CRUD de usuarios del sistema. Restringido a rol ADMIN (ver SecurityConfig).
 *
 * Este controlador se distingue del resto porque necesita hashear contraseñas.
 * Por eso inyecta PasswordEncoder directamente: al crear o actualizar un usuario,
 * la contraseña se convierte a BCrypt ANTES de persistirla en BD.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private EmpresaService empresaService;

    // El mismo encoder configurado en SecurityConfig (BCryptPasswordEncoder).
    // Inyectarlo aquí garantiza que usamos exactamente el mismo algoritmo que
    // Spring Security usa para verificar contraseñas al hacer login.
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("usuarios", usuarioService.getAll());
        return "users/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", null);
        model.addAttribute("empresas", empresaService.getAll());
        return "users/form";
    }

    /**
     * Crea un nuevo usuario hasheando la contraseña antes de guardar.
     * La contraseña en texto plano NUNCA llega a la BD.
     */
    @PostMapping("/new")
    public String create(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam(required = false) String contrasena,
            @RequestParam String rol,
            @RequestParam(required = false) Integer cod_empresa) {

        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode(contrasena));  // hashear antes de persistir
        u.setRol(rol);
        u.setCodEmpresa(cod_empresa);
        usuarioService.save(u);
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Usuario> opt = usuarioService.getById(id);
        if (opt.isEmpty()) return "redirect:/users";
        model.addAttribute("user", opt.get());
        model.addAttribute("empresas", empresaService.getAll());
        return "users/form";
    }

    /**
     * Actualiza los datos del usuario. Si la contraseña viene vacía en el formulario,
     * se mantiene la contraseña anterior sin cambios. Si viene con valor, se hashea.
     * Esto permite cambiar nombre/rol sin obligar al admin a re-ingresar la contraseña.
     */
    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam(required = false) String contrasena,
            @RequestParam String rol,
            @RequestParam(required = false) Integer cod_empresa) {

        Optional<Usuario> opt = usuarioService.getById(id);
        if (opt.isEmpty()) return "redirect:/users";
        Usuario u = opt.get();
        u.setNombre(nombre);
        u.setCorreo(correo);
        // Solo actualizar la contraseña si el admin ingresó una nueva
        if (contrasena != null && !contrasena.isEmpty()) u.setContrasena(passwordEncoder.encode(contrasena));
        u.setRol(rol);
        u.setCodEmpresa(cod_empresa);
        usuarioService.save(u);
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return "redirect:/users";
    }
}
