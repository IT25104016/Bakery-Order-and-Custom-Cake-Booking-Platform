package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
     // PRODUCT NAME==================
    @Column(nullable = false, length = 150)
    // NOT NULL + max 150 characters
    private String name;
     // PRODUCT PRICE=============
    @Column(nullable = false)
    private double price;
     // STOCK QUANTITY=================
    @Column(nullable = false)
    private int stock;

    // ── Product Category ─────────────────────────────
    @Enumerated(EnumType.STRING)
    // Stores enum as STRING in database (e.g. "CAKE", "BREAD")
    @Column(nullable = false)
    private ProductCategory category;
 // PRODUCT IMAGE======================
    @Column(length = 255)
    private String image = "default.svg";

}
