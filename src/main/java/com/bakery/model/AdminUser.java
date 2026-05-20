package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AdminUser — Inheritance from User
 * Has full system access: manage products, users, and all orders.
 */
@Entity
@DiscriminatorValue("ADMIN")//This identifier means that this class belongs to the ADMIN type in the database.
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AdminUser extends User {

    // ── Polymorphism: Override base class methods ──────────────
    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getDashboardUrl() {
        return "/admin/dashboard";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, Admin " + getName() + "! You have full system access.";
    }

    // ── Admin-specific methods ─────────────────────────────────
    public boolean canDeleteUsers() {
        return true;
    }

    public boolean canViewAllOrders() {
        return true;
    }

}
