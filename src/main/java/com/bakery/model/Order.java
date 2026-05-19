package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.Pending;

    // ── Payment method ─────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CashOnDelivery;

    // ── Delivery address ───────────────────────────────────────
    @Column(name = "delivery_address", length = 300)
    private String deliveryAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    public enum Status {
        Pending, Preparing, Ready, Completed
    }

    public enum PaymentMethod {
        CashOnDelivery, Card;

        public String getLabel() {
            return switch (this) {
                case CashOnDelivery -> "Cash on Delivery";
                case Card           -> "Card Payment";
            };
        }
    }
}
