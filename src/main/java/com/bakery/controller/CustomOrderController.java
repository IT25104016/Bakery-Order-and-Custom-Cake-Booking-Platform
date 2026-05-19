package com.bakery.controller;

import com.bakery.model.CustomOrder;
import com.bakery.model.User;
import com.bakery.service.CustomOrderService;
import com.bakery.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/customer/custom-order")
public class CustomOrderController {

    private final CustomOrderService customOrderService;
    private final UserService userService;

    public CustomOrderController(CustomOrderService customOrderService,
                                 UserService userService) {
        this.customOrderService = customOrderService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public String customOrderForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("userName", user.getName());
        model.addAttribute("profilePic", user.getProfilePic());
        model.addAttribute("sizes", CustomOrder.CakeSize.values());
        model.addAttribute("flavors", CustomOrder.CakeFlavor.values());
        model.addAttribute("minDate", LocalDate.now().plusDays(2));
        return "customer/custom-order";
    }

    @PostMapping
    public String submitCustomOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String size,
            @RequestParam String flavor,
            @RequestParam(required = false) String customMessage,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate,
            @RequestParam(required = false) String specialInstructions,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(userDetails);
        CustomOrder order = customOrderService.placeCustomOrder(
                user,
                CustomOrder.CakeSize.valueOf(size),
                CustomOrder.CakeFlavor.valueOf(flavor),
                customMessage,
                deliveryDate,
                specialInstructions
        );

        redirectAttributes.addFlashAttribute("orderId", order.getId());
        redirectAttributes.addFlashAttribute("orderPrice", order.getPrice());
        return "redirect:/customer/custom-order/success";
    }

    @GetMapping("/success")
    public String success(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("userName", user.getName());
        model.addAttribute("profilePic", user.getProfilePic());
        model.addAttribute("myOrders", customOrderService.getOrdersByUser(user));
        return "customer/custom-order-success";
    }

    @GetMapping("/my-orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("userName", user.getName());
        model.addAttribute("profilePic", user.getProfilePic());
        model.addAttribute("orders", customOrderService.getOrdersByUser(user));
        return "customer/my-custom-orders";
    }
}
