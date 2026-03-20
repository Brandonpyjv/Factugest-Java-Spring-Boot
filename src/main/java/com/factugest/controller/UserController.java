package com.factugest.controller;

import com.factugest.entity.Usuario;
import com.factugest.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("usuarios", usuarioService.getAll());
        return "users/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", null);
        return "users/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam(required = false) String contrasena,
            @RequestParam String rol) {

        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode(contrasena));
        u.setRol(rol);
        usuarioService.save(u);
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Usuario> opt = usuarioService.getById(id);
        if (opt.isEmpty()) return "redirect:/users";
        model.addAttribute("user", opt.get());
        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam(required = false) String contrasena,
            @RequestParam String rol) {

        Optional<Usuario> opt = usuarioService.getById(id);
        if (opt.isEmpty()) return "redirect:/users";
        Usuario u = opt.get();
        u.setNombre(nombre);
        u.setCorreo(correo);
        if (contrasena != null && !contrasena.isEmpty()) u.setContrasena(passwordEncoder.encode(contrasena));
        u.setRol(rol);
        usuarioService.save(u);
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return "redirect:/users";
    }
}
