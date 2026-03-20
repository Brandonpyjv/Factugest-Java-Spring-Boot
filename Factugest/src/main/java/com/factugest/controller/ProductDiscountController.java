package com.factugest.controller;

import com.factugest.service.ProductDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product_discounts")
public class ProductDiscountController {

    @Autowired private ProductDiscountService productDiscountService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("all_product_discounts", productDiscountService.getAll());
        return "product_discounts/index";
    }
}
