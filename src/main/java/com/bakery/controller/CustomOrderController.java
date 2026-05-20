// open the oreder form, submit custom cake order form,submit custom cake order,view placed order
package com.bakery.controller;
//impports the model class
import com.bakery.model.CustomOrder;
import com.bakery.model.User;
// imports the service class
import com.bakery.service.CustomOrderService;
import com.bakery.service.UserService;
// date formatting
import org.springframework.format.annotation.DateTimeFormat;
// gets logged in user
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
//spring mvc imports
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//Flash messages
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/customer/custom-order")
public class CustomOrderController {
    //dependency
    private final CustomOrderService customOrderService;
    private final UserService userService;
    //constructor
    public CustomOrderController(CustomOrderService customOrderService,
                                 UserService userService) {
        this.customOrderService = customOrderService;
        this.userService = userService;
    }
//used to get the details of the users logged in
    //find the user using the email
    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername()).orElseThrow();
    }
//opens the custom order form page
    @GetMapping
    public String customOrderForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);//logged in user details
        model.addAttribute("userName", user.getName());// sends data to the frot end
        model.addAttribute("profilePic", user.getProfilePic());
        model.addAttribute("sizes", CustomOrder.CakeSize.values());// drop down menu value
        model.addAttribute("flavors", CustomOrder.CakeFlavor.values());
        model.addAttribute("minDate", LocalDate.now().plusDays(2));
        return "customer/custom-order";
    }

    @PostMapping // handles the form submission
    public String submitCustomOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            //Gets the data from the users
            @RequestParam String size,
            @RequestParam String flavor,
            @RequestParam(required = false) String customMessage,// keeping blank doesnt gives an error
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate,
            @RequestParam(required = false) String specialInstructions,
            RedirectAttributes redirectAttributes) {
    // gets the details of the logged in user
        User user = getCurrentUser(userDetails);// takes the logged in user info and create the object
        CustomOrder order = customOrderService.placeCustomOrder(
                user,
                CustomOrder.CakeSize.valueOf(size),
                CustomOrder.CakeFlavor.valueOf(flavor),
                customMessage,
                deliveryDate,
                specialInstructions
                //convert the input to proper types and send everything to the service to create and save a custom cake order in the database
        );
//redirects and give the success message
        redirectAttributes.addFlashAttribute("orderId", order.getId());// temperory data stoed only for the next page
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
