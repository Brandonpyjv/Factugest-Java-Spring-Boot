package com.factugest.controller;

import com.factugest.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador del dashboard principal y la pantalla de configuración.
 *
 * Es el primer lugar que ve el usuario tras autenticarse. Consulta las
 * estadísticas del negocio (facturas, clientes, ingresos, etc.) y las
 * pone disponibles en el modelo para que Thymeleaf las renderice.
 */
@Controller
public class HomeController {

    // InvoiceService centraliza la lógica de negocio de facturas,
    // incluyendo las consultas de resumen para el dashboard.
    @Autowired private InvoiceService invoiceService;

    /**
     * Dashboard principal: muestra KPIs del negocio en tiempo real.
     * Model es el "mapa de datos" que se pasa a la vista — cada clave
     * del modelo queda disponible en Thymeleaf como ${stats}.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("stats", invoiceService.getDashboardStats());
        return "index"; // → templates/index.html
    }

    /**
     * Pantalla de configuración general. Por ahora solo muestra la vista
     * sin lógica adicional; candidata a expandirse con ajustes de empresa.
     */
    @GetMapping("/settings")
    public String settings() {
        return "settings/index"; // → templates/settings/index.html
    }
}
