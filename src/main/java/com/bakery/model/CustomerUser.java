package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * CustomerUser — Inheritance from User
 * Regular customer: browse products, place orders, custom cake orders
 */
@Entity
@DiscriminatorValue("CUSTOMER")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CustomerUser extends User {

    // ── Polymorphism: Override base class methods ──────────────
    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public String getDashboardUrl() {
        return "/customer/products";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, " + getName() + "! Explore our fresh bakes!";
    }

    // ── Customer-specific methods ──────────────────────────────
    public boolean canPlaceOrder() {
        return true;
    }

    public boolean canPlaceCustomOrder() {
        return true;
    }

    public boolean canViewOwnOrders() {
        return true;
    }
}
