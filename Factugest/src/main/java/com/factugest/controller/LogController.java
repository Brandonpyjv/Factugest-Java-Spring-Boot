package com.factugest.controller;

import com.factugest.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/logs")
public class LogController {

    @Autowired private LogService logService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("logs", logService.getAll());
        return "logs/index";
    }
}
