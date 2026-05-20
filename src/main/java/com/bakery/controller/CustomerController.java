package com.bakery.controller;

import com.bakery.model.*;
import com.bakery.service.OrderService;
import com.bakery.service.ProductRatingService;
import com.bakery.service.ProductService;
import com.bakery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRatingService ratingService;

    public CustomerController(UserService userService,
                              ProductService productService,
                              OrderService orderService,
                              ProductRatingService ratingService) {

        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.ratingService = ratingService;
    }

    // ── Current Logged User ────────────────────────────────────
    private User getCurrentUser(UserDetails userDetails) {

        return userService.findByEmail(
                userDetails.getUsername()
        ).orElseThrow();
    }

    // ── Products Page ──────────────────────────────────────────
    @GetMapping("/products")
    public String products(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            ProductCategory category,

            Model model
    ) {

        List<Product> products;

        // Search + Category
        if (search != null
                && !search.isBlank()) {

            products =
                    productService
                            .searchAvailable(search);

            if (category != null) {

                products = products.stream()

                        .filter(product ->
                                product.getCategory()
                                        == category
                        )

                        .toList();
            }
        }

        // Category only
        else if (category != null) {

            products =
                    productService
                            .getByCategory(category);
        }

        // All products
        else {

            products =
                    productService
                            .getAvailableProducts();
        }

        // Logged user
        if (userDetails != null) {

            User user =
                    getCurrentUser(userDetails);

            model.addAttribute(
                    "userName",
                    user.getName()
            );

            model.addAttribute(
                    "profilePic",
                    user.getProfilePic()
            );
        }

        Map<ProductCategory, List<Product>> productsByCategory =
                new LinkedHashMap<>();

        for (ProductCategory productCategory : ProductCategory.values()) {

            List<Product> categoryProducts =
                    products.stream()

                            .filter(product ->
                                    product.getCategory()
                                            == productCategory
                            )

                            .toList();

            if (!categoryProducts.isEmpty()) {

                productsByCategory.put(
                        productCategory,
                        categoryProducts
                );
            }
        }

        model.addAttribute(
                "products",
                products
        );

        model.addAttribute(
                "productsByCategory",
                productsByCategory
        );

        model.addAttribute(
                "search",
                search
        );

        model.addAttribute(
                "selectedCategory",
                category
        );

        model.addAttribute(
                "categories",
                ProductCategory.values()
        );

        model.addAttribute(
                "avgStarsMap",
                ratingService.getAverageStarsMap(products)
        );

        model.addAttribute(
                "ratingCountMap",
                ratingService.getRatingCountMap(products)
        );

        return "customer/products";
    }

    // ── Add To Cart ────────────────────────────────────────────
    @PostMapping("/cart/add")
    @SuppressWarnings("unchecked")
    public String addToCart(@RequestParam int productId,
                            @RequestParam int quantity,
                            HttpSession session) {

        Product product =
                productService.findById(productId)
                        .orElseThrow();

        List<CartItem> cart =
                (List<CartItem>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        boolean found = false;

        for (CartItem item : cart) {

            if (item.getProductId() == productId) {

                item.setQuantity(
                        item.getQuantity() + quantity
                );

                found = true;

                break;
            }
        }

        if (!found) {

            cart.add(
                    new CartItem(
                            productId,
                            product.getName(),
                            product.getPrice(),
                            quantity,
                            product.getImage()
                    )
            );
        }

        session.setAttribute("cart", cart);

        return "redirect:/customer/cart";
    }

    // ── View Cart ──────────────────────────────────────────────
    @GetMapping("/cart")
    @SuppressWarnings("unchecked")
    public String viewCart(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                getCurrentUser(userDetails);

        List<CartItem> cart =
                (List<CartItem>) session.getAttribute("cart");

        double grandTotal =
                cart != null
                        ? cart.stream()
                        .mapToDouble(CartItem::getTotal)
                        .sum()
                        : 0;

        model.addAttribute(
                "userName",
                user.getName()
        );

        model.addAttribute(
                "profilePic",
                user.getProfilePic()
        );

        model.addAttribute(
                "cart",
                cart
        );

        model.addAttribute(
                "grandTotal",
                grandTotal
        );

        return "customer/cart";
    }

    // ── Remove Cart Item ───────────────────────────────────────
    @PostMapping("/cart/remove")
    @SuppressWarnings("unchecked")
    public String removeFromCart(
            @RequestParam int productId,
            HttpSession session
    ) {

        List<CartItem> cart =
                (List<CartItem>) session.getAttribute("cart");

        if (cart != null) {

            cart.removeIf(
                    item -> item.getProductId() == productId
            );

            session.setAttribute("cart", cart);
        }

        return "redirect:/customer/cart";
    }

    // ── Checkout Page ──────────────────────────────────────────
    @GetMapping("/checkout")
    @SuppressWarnings("unchecked")
    public String checkoutPage(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                getCurrentUser(userDetails);

        List<CartItem> cart =
                (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {

            return "redirect:/customer/cart";
        }

        double grandTotal =
                cart.stream()
                        .mapToDouble(CartItem::getTotal)
                        .sum();

        model.addAttribute(
                "userName",
                user.getName()
        );

        model.addAttribute(
                "profilePic",
                user.getProfilePic()
        );

        model.addAttribute(
                "cart",
                cart
        );

        model.addAttribute(
                "grandTotal",
                grandTotal
        );

        model.addAttribute(
                "paymentMethods",
                Order.PaymentMethod.values()
        );

        return "customer/checkout";
    }

    // ── Place Order ────────────────────────────────────────────
    @PostMapping("/checkout")
    @SuppressWarnings("unchecked")
    public String placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String deliveryAddress,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        List<CartItem> cart =
                (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {

            return "redirect:/customer/cart?empty=true";
        }

        User user =
                getCurrentUser(userDetails);

        Map<Integer, Product> productMap =
                new HashMap<>();

        for (CartItem item : cart) {

            productService.findById(item.getProductId())
                    .ifPresent(p ->
                            productMap.put(p.getId(), p));
        }

        Order.PaymentMethod pm =
                Order.PaymentMethod.CashOnDelivery;

        try {

            if (paymentMethod != null) {

                pm = Order.PaymentMethod.valueOf(paymentMethod);
            }

        } catch (Exception ignored) {

            pm = Order.PaymentMethod.CashOnDelivery;
        }

        Order order;

        try {

            order =
                    orderService.placeOrderWithProducts(
                            user,
                            cart,
                            productMap,
                            pm,
                            deliveryAddress
                    );

        } catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "stockError",
                    e.getMessage()
            );

            return "redirect:/customer/cart";
        }

        session.removeAttribute("cart");

        session.setAttribute(
                "lastOrderId",
                order.getId()
        );

        return "redirect:/customer/order-success";
    }

    // ── Order Success ──────────────────────────────────────────
    @GetMapping("/order-success")
    public String orderSuccess(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                getCurrentUser(userDetails);

        model.addAttribute(
                "userName",
                user.getName()
        );

        model.addAttribute(
                "profilePic",
                user.getProfilePic()
        );

        model.addAttribute(
                "lastOrderId",
                session.getAttribute("lastOrderId")
        );

        model.addAttribute(
                "orders",
                orderService.getOrdersByUser(user)
        );

        return "customer/order-success";
    }

    // ── My Orders ──────────────────────────────────────────────
    @GetMapping("/my-orders")
    public String myOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                getCurrentUser(userDetails);

        model.addAttribute(
                "userName",
                user.getName()
        );

        model.addAttribute(
                "profilePic",
                user.getProfilePic()
        );

        model.addAttribute(
                "orders",
                orderService.getOrdersByUser(user)
        );

        return "customer/my-orders";
    }

    // ── Profile Page ───────────────────────────────────────────
    @GetMapping("/profile")
    public String profilePage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                getCurrentUser(userDetails);

        model.addAttribute(
                "userName",
                user.getName()
        );

        model.addAttribute(
                "profilePic",
                user.getProfilePic()
        );

        model.addAttribute(
                "user",
                user
        );

        return "customer/profile";
    }

    // ── Update Profile ─────────────────────────────────────────
    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String name,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) MultipartFile profilePicFile,
            RedirectAttributes redirectAttributes
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                getCurrentUser(userDetails);

        userService.updateProfile(
                user.getId(),
                name,
                newPassword
        );

        if (profilePicFile != null
                && !profilePicFile.isEmpty()) {

            try {

                userService.updateProfilePic(
                        user.getId(),
                        profilePicFile
                );

            } catch (Exception e) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Profile pic upload failed: "
                                + e.getMessage()
                );

                return "redirect:/customer/profile";
            }
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Profile updated successfully!"
        );

        return "redirect:/customer/profile";
    }

    // DELETE
    @PostMapping("/profile/delete-pic")
    public String deleteProfilePic(
            @AuthenticationPrincipal UserDetails userDetails, //Gets currently logged-in user details from Spring Security
            RedirectAttributes redirectAttributes //send temporary success/error messages after redirect
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                getCurrentUser(userDetails);

        userService.deleteProfilePic(user.getId());

        redirectAttributes.addFlashAttribute(
                "success",
                "Profile picture deleted!"
        );

        return "redirect:/customer/profile";
    }
}
