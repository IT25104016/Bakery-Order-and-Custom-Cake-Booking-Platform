package com.bakery.controller;

import com.bakery.model.Product;
import com.bakery.model.ProductRating;
import com.bakery.model.User;
import com.bakery.service.ProductRatingService;
import com.bakery.service.ProductService;
import com.bakery.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer/rating")
public class ProductRatingController {

    private final ProductRatingService ratingService;
    private final ProductService productService;
    private final UserService userService;

    public ProductRatingController(ProductRatingService ratingService,
                                   ProductService productService,
                                   UserService userService) {
        this.ratingService  = ratingService;
        this.productService = productService;
        this.userService    = userService;
    }

    private User getCurrentUser(UserDetails ud) {
        return userService.findByEmail(ud.getUsername()).orElseThrow();
    }

    // ── Product detail + ratings page ─────────────────────────
    @GetMapping("/product/{productId}")
    public String productDetail(@AuthenticationPrincipal UserDetails ud,
                                @PathVariable int productId,
                                Model model) {
        User user       = getCurrentUser(ud);
        Product product = productService.findById(productId).orElseThrow();

        List<ProductRating> ratings = ratingService.getRatingsForProduct(product);
        Optional<ProductRating> myRating = ratingService.getUserRatingForProduct(user, product);

        model.addAttribute("userName",   user.getName());
        model.addAttribute("profilePic", user.getProfilePic());
        model.addAttribute("product",    product);
        model.addAttribute("ratings",    ratings);
        model.addAttribute("myRating",   myRating.orElse(null));
        model.addAttribute("avgStars",   ratingService.getAverageStars(product));
        model.addAttribute("ratingCount",ratingService.getRatingCount(product));
        return "customer/product-detail";
    }
    @PostMapping("/delete")
    public String deleteRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int productId,
            RedirectAttributes redirectAttributes
    ) {

        if (userDetails == null) {

            return "redirect:/login";
        }

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                ).orElseThrow();

        ratingService.deleteRating(
                user.getId(),
                productId
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Rating deleted successfully!"
        );

        return "redirect:/customer/rating/product/"
                + productId;
    }

    // ── Submit / update rating ─────────────────────────────────
    @PostMapping("/submit")
    public String submitRating(@AuthenticationPrincipal UserDetails ud,
                               @RequestParam int productId,
                               @RequestParam int stars,
                               @RequestParam(required = false) String review,
                               RedirectAttributes ra) {
        User user       = getCurrentUser(ud);
        Product product = productService.findById(productId).orElseThrow();
        ratingService.submitRating(user, product, stars, review);
        ra.addFlashAttribute("success", "thank you for submitting Rating! ");
        return "redirect:/customer/rating/product/" + productId;
    }
}
