package com.bakery.controller;

import com.bakery.model.Order;
import com.bakery.model.Product;
import com.bakery.model.ProductCategory;
import com.bakery.model.User;
import com.bakery.service.ImageUploadService;
import com.bakery.service.OrderService;
import com.bakery.service.ProductService;
import com.bakery.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller//A controller handles HTTP requests and returns responses/views.(Marks this class as a Spring MVC controlle)
@RequestMapping("/admin")//All URLs in this controller start with/admin
public class AdminController {

    private final UserService userService;//Association relation
    private final ProductService productService;
    private final OrderService orderService;
    private final ImageUploadService imageUploadService;

    public AdminController(UserService userService,//AdminController HAS-A UserService(Composition)internally services hold
                           ProductService productService,
                           OrderService orderService,
                           ImageUploadService imageUploadService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.imageUploadService = imageUploadService;
    }

    @GetMapping("/dashboard")//Handles HTTP GET requests
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalOrders", orderService.count());
        model.addAttribute("totalUsers", userService.countCustomers());
        model.addAttribute("totalRevenue", orderService.totalRevenue());
        model.addAttribute("pendingOrders", orderService.pendingOrders());
        model.addAttribute("topSellingItems", orderService.topSellingItems(5));
        model.addAttribute("lowStockItems", productService.getAllLowStock());
        model.addAttribute("lowStockCount", productService.getAllLowStock().size());
        model.addAttribute("currentUser",
                userService.findByEmail(userDetails.getUsername())
                        .map(User::getName)
                        .orElse(userDetails.getUsername()));
        return "admin/dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = getCurrentAdmin(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", user.getName());
        return "admin/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String name,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) MultipartFile profilePicFile,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentAdmin(userDetails);

        userService.updateProfile(user.getId(), name, newPassword);

        if (profilePicFile != null && !profilePicFile.isEmpty()) {
            try {
                userService.updateProfilePic(user.getId(), profilePicFile);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Profile pic upload failed: " + e.getMessage()
                );
                return "redirect:/admin/profile";
            }
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Profile updated successfully!"
        );

        return "redirect:/admin/profile";
    }

    @GetMapping("/users")
    public String viewUsers(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElse(null);

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentAdminIsMain", userService.isMainAdmin(currentUser));
        model.addAttribute("mainAdminId", UserService.MAIN_ADMIN_ID);
        model.addAttribute("mainAdminEmail", UserService.MAIN_ADMIN_EMAIL);
        return "admin/users";
    }


    @GetMapping("/user/delete/{id}")
    public String deleteUser(

            @AuthenticationPrincipal UserDetails userDetails,

            @PathVariable int id,

            RedirectAttributes redirectAttributes

    ) {
   //Exception Handling
        try {

            User user =
                    userService.findById(id)
                            .orElseThrow(() -> new IllegalStateException("User not found."));

            User currentUser = //Inheritance
                    userService.findByEmail(userDetails.getUsername())//Polymorphism
                            .orElseThrow(() -> new IllegalStateException("Current admin account not found."));

            if (!userService.canDeleteUser(currentUser, user)) {

                redirectAttributes.addFlashAttribute(

                        "error",

                        getDeleteDeniedMessage(currentUser, user)
                );

                return "redirect:/admin/users";
            }

            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute(

                    "success",

                    "User deleted successfully!"
            );

        }

        catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute(

                    "error",

                    e.getMessage()
            );
        }

        return "redirect:/admin/users";
    }



    @GetMapping("/products")
    public String viewProducts(@RequestParam(required = false) String search,
                               Model model) {
        model.addAttribute("products",
                search != null && !search.isBlank()
                        ? productService.searchAll(search)
                        : productService.getAllProducts());
        model.addAttribute("search", search);
        return "admin/products";
    }

    @GetMapping("/product/add")
    public String addProductForm() {
        return "admin/add-product";
    }

    @PostMapping("/product/add")
    public String addProduct(

            @RequestParam String name,

            @RequestParam double price,

            @RequestParam int stock,

            @RequestParam ProductCategory category,

            @RequestParam(required = false)
            MultipartFile imageFile,

            RedirectAttributes redirectAttributes
    ) {

        String filename =
                resolveImage(
                        imageFile,
                        null,
                        redirectAttributes
                );

        if (filename == null) {

            return "redirect:/admin/product/add";
        }

        productService.saveProduct(

                name,

                price,

                stock,

                category,

                filename
        );

        redirectAttributes.addFlashAttribute(

                "success",

                "Product added successfully!"
        );

        return "redirect:/admin/products";
    }

    @GetMapping("/create-admin")
    public String createAdminPage() {

        return "admin/create-admin";
    }

    @GetMapping("/product/edit/{id}")
    public String editProductForm(@PathVariable int id, Model model) {
        model.addAttribute("product", productService.findById(id).orElseThrow());
        return "admin/edit-product";
    }




    @PostMapping("/create-admin")
    public String createAdmin(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes ra
    ) {

        if (userService.existsByEmail(email)) {

            ra.addFlashAttribute(
                    "error",
                    "Email already exists."
            );

            return "redirect:/admin/create-admin";
        }

        userService.createAdmin(
                name,
                email,
                password
        );

        ra.addFlashAttribute(
                "success",
                "Admin created successfully!"
        );

        return "redirect:/admin/create-admin";
    }



    @PostMapping("/product/edit/{id}")
    public String editProduct(@PathVariable int id,
                              @RequestParam String name,
                              @RequestParam double price,
                              @RequestParam int stock,
                              @RequestParam(required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        Product existing = productService.findById(id).orElseThrow();
        String filename = resolveImage(imageFile, existing.getImage(), redirectAttributes);
        if (filename == null) {
            return "redirect:/admin/product/edit/" + id;
        }

        productService.updateProduct(id, name, price, stock, filename);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable int id,
                                RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("statuses", Order.Status.values());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable int id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, Order.Status.valueOf(status));
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    private String resolveImage(MultipartFile file, String existingImage, RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            return existingImage != null && !existingImage.isBlank() ? existingImage : "default.svg";
        }

        try {
            return imageUploadService.uploadProductImage(file);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return null;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Image upload failed. Please try again.");
            return null;
        }
    }

    private String getDeleteDeniedMessage(User currentUser, User targetUser) {
        if (currentUser.getId() == targetUser.getId()) {
            return "You cannot delete your own account.";
        }

        if (userService.isMainAdmin(targetUser)) {
            return "Main admin cannot be deleted.";
        }

        if ("ADMIN".equals(targetUser.getRole())) {
            return "Only the main admin can delete other admins.";
        }

        return "You are not allowed to delete this user.";
    }
}
