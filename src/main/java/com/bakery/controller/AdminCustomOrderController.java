package com.bakery.controller;

import com.bakery.model.CustomOrder;
import com.bakery.service.CustomOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/custom-orders")
public class AdminCustomOrderController {

    private final CustomOrderService customOrderService;

    public AdminCustomOrderController(CustomOrderService customOrderService) {
        this.customOrderService = customOrderService;
    }

    @GetMapping
    public String viewAll(Model model) {
        model.addAttribute("orders", customOrderService.getAllOrders());
        model.addAttribute("statuses", CustomOrder.CustomOrderStatus.values());
        return "admin/custom-orders";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable int id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        customOrderService.updateStatus(id, CustomOrder.CustomOrderStatus.valueOf(status));
        redirectAttributes.addFlashAttribute("success", "Custom order status updated successfully!");
        return "redirect:/admin/custom-orders";
    }
}
