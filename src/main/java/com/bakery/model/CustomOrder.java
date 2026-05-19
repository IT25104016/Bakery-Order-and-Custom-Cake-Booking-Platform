package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "custom_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CakeSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CakeFlavor flavor;

    @Column(name = "custom_message", length = 200)
    private String customMessage;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomOrderStatus status = CustomOrderStatus.Pending;

    @Column(nullable = false)
    private double price;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Enums ──────────────────────────────────────────────────

    public enum CakeSize {
        Small, Medium, Large;

        public double getBasePrice() {
            return switch (this) {
                case Small  -> 1200.00;
                case Medium -> 2200.00;
                case Large  -> 3500.00;
            };
        }

        public String getLabel() {
            return switch (this) {
                case Small  -> "Small (500g) - Rs. 1,200";
                case Medium -> "Medium (1kg) - Rs. 2,200";
                case Large  -> "Large (2kg) - Rs. 3,500";
            };
        }
    }

    public enum CakeFlavor {
        Chocolate, Vanilla, RedVelvet, Strawberry, Caramel, BlackForest;

        public String getLabel() {
            return switch (this) {
                case Chocolate   -> "Chocolate";
                case Vanilla     -> "Vanilla";
                case RedVelvet   -> "Red Velvet";
                case Strawberry  -> "Strawberry";
                case Caramel     -> "Caramel";
                case BlackForest -> "Black Forest";
            };
        }
    }

    public enum CustomOrderStatus {
        Pending, Confirmed, Baking, Ready, Completed, Cancelled
    }
}
