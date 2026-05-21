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
import org.springframework.ui.Model;// Used to send data from controller to frontend view
import org.springframework.web.bind.annotation.GetMapping;// Handles HTTP GET requests
import org.springframework.web.bind.annotation.PathVariable;// Gets values from URL path
import org.springframework.web.bind.annotation.PostMapping;// Handles HTTP POST requests
import org.springframework.web.bind.annotation.RequestMapping;// Defines base URL mapping for controller
import org.springframework.web.bind.annotation.RequestParam;// Gets form/request parameter values
import org.springframework.web.multipart.MultipartFile;// Used for file uploads
import org.springframework.web.servlet.mvc.support.RedirectAttributes;// Used for sending flash messages during redirects

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


@GetMapping("/profile") // Handles request for loading admin profile page
public String profilePage(@AuthenticationPrincipal UserDetails userDetails,
                          Model model) {

    // Get currently logged-in admin user
    User user = getCurrentAdmin(userDetails);

    // Send user object to frontend
    model.addAttribute("user", user);

    // Send current user's name separately
    model.addAttribute("currentUser", user.getName());

    // Return profile page
    return "admin/profile";
}

@PostMapping("/profile") // Handles profile update form submission
public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam String name,
                            @RequestParam(required = false) String newPassword,
                            @RequestParam(required = false) MultipartFile profilePicFile,
                            RedirectAttributes redirectAttributes) {

    // Get currently logged-in admin user
    User user = getCurrentAdmin(userDetails);

    // Update name and password
    userService.updateProfile(user.getId(), name, newPassword);

    // Check whether profile picture file exists
    if (profilePicFile != null && !profilePicFile.isEmpty()) {

        try {

            // Upload and update profile picture
            userService.updateProfilePic(user.getId(), profilePicFile);

        } catch (Exception e) {

            // Show error message if upload fails
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Profile pic upload failed: " + e.getMessage()
            );

            // Redirect back to profile page
            return "redirect:/admin/profile";
        }
    }

    // Show success message after update
    redirectAttributes.addFlashAttribute(
            "success",
            "Profile updated successfully!"
    );

    // Redirect back to profile page
    return "redirect:/admin/profile";
}



  
@GetMapping("/users") // Handles request for viewing all users
public String viewUsers(@AuthenticationPrincipal UserDetails userDetails,
                        Model model) {

    // Get currently logged-in user using email/username
    User currentUser = userService.findByEmail(userDetails.getUsername())
            .orElse(null);

    // Send all users list to the frontend page
    model.addAttribute("users", userService.getAllUsers());

    // Check whether current admin is the main admin
    model.addAttribute("currentAdminIsMain", userService.isMainAdmin(currentUser));

    // Send main admin ID to the frontend
    model.addAttribute("mainAdminId", UserService.MAIN_ADMIN_ID);

    // Send main admin email to the frontend
    model.addAttribute("mainAdminEmail", UserService.MAIN_ADMIN_EMAIL);

    // Return admin users page
    return "admin/users";
}




 
@GetMapping("/user/delete/{id}") // Handles GET request for deleting a user by ID
public String deleteUser(

        @AuthenticationPrincipal UserDetails userDetails, // Gets currently logged-in user's details

        @PathVariable int id, // Gets user ID from URL path

        RedirectAttributes redirectAttributes // Used to send success/error messages after redirect

) {

    try {

        // Find the user that needs to be deleted
        User user =
                userService.findById(id)
                        .orElseThrow(() -> new IllegalStateException("User not found."));

        // Find the currently logged-in admin user
        User currentUser =
                userService.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new IllegalStateException("Current admin account not found."));

        // Check whether the current admin is allowed to delete this user
        if (!userService.canDeleteUser(currentUser, user)) {

            // Add error message if deletion is not allowed
            redirectAttributes.addFlashAttribute(

                    "error",

                    getDeleteDeniedMessage(currentUser, user)
            );

            // Redirect back to admin users page
            return "redirect:/admin/users";
        }

        // Delete the selected user
        userService.deleteUser(id);

        // Add success message after successful deletion
        redirectAttributes.addFlashAttribute(

                "success",

                "User deleted successfully!"
        );

    }

    catch (IllegalStateException e) {

        // Handle exceptions and show error message
        redirectAttributes.addFlashAttribute(

                "error",

                e.getMessage()
        );
    }

    // Redirect back to admin users page
    return "redirect:/admin/users";
}





  
@GetMapping("/products") // Handles request for viewing all products
public String viewProducts(@RequestParam(required = false) String search,
                           Model model) {

    // If search value exists, search products. Otherwise get all products.
    model.addAttribute("products",
            search != null && !search.isBlank()
                    ? productService.searchAll(search)
                    : productService.getAllProducts());

    // Send search keyword back to frontend
    model.addAttribute("search", search);

    // Return products page
    return "admin/products";
}



@GetMapping("/product/add") // Loads add product form page
public String addProductForm() {

    return "admin/add-product";
}



@PostMapping("/product/add") // Handles add product form submission
public String addProduct(

        @RequestParam String name, // Product name

        @RequestParam double price, // Product price

        @RequestParam int stock, // Product stock quantity

        @RequestParam ProductCategory category, // Product category

        @RequestParam(required = false)
        MultipartFile imageFile, // Product image file

        RedirectAttributes redirectAttributes // Used for success/error messages
) {

    // Upload image and get filename
    String filename =
            resolveImage(
                    imageFile,
                    null,
                    redirectAttributes
            );

    // If image upload fails, redirect back
    if (filename == null) {

        return "redirect:/admin/product/add";
    }

    // Save product details
    productService.saveProduct(

            name,

            price,

            stock,

            category,

            filename
    );

    // Show success message
    redirectAttributes.addFlashAttribute(

            "success",

            "Product added successfully!"
    );

    // Redirect to products page
    return "redirect:/admin/products";
}



@GetMapping("/create-admin") // Loads create admin page
public String createAdminPage() {

    return "admin/create-admin";
}



@GetMapping("/product/edit/{id}") // Loads edit product form page
public String editProductForm(@PathVariable int id, Model model) {

    // Find product by ID and send to frontend
    model.addAttribute("product", productService.findById(id).orElseThrow());

    // Return edit product page
    return "admin/edit-product";
}






    
@PostMapping("/create-admin") // Handles admin creation form submission
public String createAdmin(
        @RequestParam String name, // Get admin name from form
        @RequestParam String email, // Get admin email from form
        @RequestParam String password, // Get admin password from form
        RedirectAttributes ra // Used for success/error messages
) {

    // Check whether email already exists
    if (userService.existsByEmail(email)) {

        // Show error message if email is already used
        ra.addFlashAttribute(
                "error",
                "Email already exists."
        );

        // Redirect back to create admin page
        return "redirect:/admin/create-admin";
    }

    // Create new admin account
    userService.createAdmin(
            name,
            email,
            password
    );

    // Show success message
    ra.addFlashAttribute(
            "success",
            "Admin created successfully!"
    );

    // Redirect back to create admin page
    return "redirect:/admin/create-admin";
}



@PostMapping("/product/edit/{id}") // Handles product update request
public String editProduct(@PathVariable int id,
                          @RequestParam String name,
                          @RequestParam double price,
                          @RequestParam int stock,
                          @RequestParam(required = false) MultipartFile imageFile,
                          RedirectAttributes redirectAttributes) {

    // Find existing product by ID
    Product existing = productService.findById(id).orElseThrow();

    // Handle image upload and get image filename
    String filename = resolveImage(imageFile, existing.getImage(), redirectAttributes);

    // If image upload fails, redirect back to edit page
    if (filename == null) {
        return "redirect:/admin/product/edit/" + id;
    }

    // Update product details
    productService.updateProduct(id, name, price, stock, filename);

    // Show success message
    redirectAttributes.addFlashAttribute("success", "Product updated successfully!");

    // Redirect to products page
    return "redirect:/admin/products";
}



@GetMapping("/product/delete/{id}") // Handles product deletion request
public String deleteProduct(@PathVariable int id,
                            RedirectAttributes redirectAttributes) {

    // Delete product using ID
    productService.deleteProduct(id);

    // Show success message
    redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");

    // Redirect to products page
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
