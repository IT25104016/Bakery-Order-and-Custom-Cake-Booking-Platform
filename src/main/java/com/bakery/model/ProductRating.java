package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_ratings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 // STAR RATING=================
    @Column(nullable = false)
    private int stars; // 1–5
// REVIEW TEXT=================
    @Column(length = 300)
    private String review;  // Optional comment/review
  // CREATED TIME===================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // Cannot be updated after creation
}
