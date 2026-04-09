package com.factugest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador mínimo para mostrar la página de login.
 *
 * Spring Security maneja el POST /login internamente (no necesita un método aquí).
 * Solo necesitamos un GET que devuelva la vista del formulario. Cuando el usuario
 * accede a /login con ?error o ?logout en la URL, Thymeleaf los detecta con
 * th:if="${param.error}" y th:if="${param.logout}" en login.html.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // resuelve a src/main/resources/templates/login.html
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "logout"; // resuelve a src/main/resources/templates/logout.html
    }
}
