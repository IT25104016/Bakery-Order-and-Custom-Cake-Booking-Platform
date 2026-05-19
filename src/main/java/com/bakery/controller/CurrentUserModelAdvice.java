package com.bakery.controller;

import com.bakery.model.User;
import com.bakery.service.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Locale;

@ControllerAdvice
public class CurrentUserModelAdvice {

    private final UserService userService;

    public CurrentUserModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addCurrentUser(Model model, Principal principal) {
        if (principal == null) {
            return;
        }

        userService.findByEmail(principal.getName())
                .ifPresent(user -> addUserAttributes(model, user));
    }

    private void addUserAttributes(Model model, User user) {
        String profilePic = user.getProfilePic();
        String name = user.getName();

        model.addAttribute("userName", user.getName());
        model.addAttribute("profilePic", profilePic);
        model.addAttribute("hasProfilePic", profilePic != null && !profilePic.isBlank());
        model.addAttribute("userInitial", getInitial(name));
    }

    private String getInitial(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }

        return name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
