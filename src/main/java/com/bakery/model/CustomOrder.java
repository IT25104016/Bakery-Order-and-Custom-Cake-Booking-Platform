package com.bakery.model;

import jakarta.persistence.*;//used for DB mapping
import lombok.Data;// automatically creates getters setters and constructors
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
//JPA(No need to manually write sql) entity used connect the java class to the data base
@Entity // data entity table
@Table(name = "custom_orders")
@Data
@NoArgsConstructor // empty constructor
@AllArgsConstructor // constructor with fields
public class CustomOrder {// aggreagtion(CustomerOrder has a user)

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // automatically genereates id
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)//one user can do manys orders(lazy - fetch user only when needed)
    @JoinColumn(name = "user_id")
    private User user;// aggregation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)//compulsory to be filled
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

    @Enumerated(EnumType.STRING)// stores the enum as texts in the DB
    @Column(nullable = false)
    private CustomOrderStatus status = CustomOrderStatus.Pending;

    @Column(nullable = false)
    private double price;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Enums ──────────────────────────────────────────────────

    public enum CakeSize {
        Small, Medium, Large;// enum values

        public double getBasePrice() {
            return switch (this) {
                case Small  -> 1200.00;// arrow key is used instead of the if
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
