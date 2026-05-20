package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AdminUser — Inheritance from User
 * Has full system access: manage products, users, and all orders.
 */
@Entity// Marks this class as a database entity (table).
@DiscriminatorValue("ADMIN")//This identifier means that this class belongs to the ADMIN type in the database.
@Data//that automatically generates getters, setters, toString(), equals(), and hashCode() methods.
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor//that automatically creates a no-argument constructor.
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
