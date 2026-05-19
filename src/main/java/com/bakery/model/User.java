package com.bakery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "profile_pic", length = 100)
    private String profilePic;

    // ── Polymorphic methods — each subclass overrides differently ──
    public abstract String getRole();
    public abstract String getDashboardUrl();
    public abstract String getWelcomeMessage();

    // ── Common method used by all subclasses ───────────────────────
    public String getDisplayInfo() {
        return "User[" + getRole() + "]: " + name + " (" + email + ")";
    }
}
