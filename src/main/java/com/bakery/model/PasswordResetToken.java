package com.bakery.model;
//E

import jakarta.persistence.*;
import lombok.Data; //Generates getters/setters automatically.
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;//built in java class to store and manipulate date and time values without timezone information.

@Entity
@Table(name = "password_reset_tokens")
@Data //Generates getters/setters automatically.
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Database auto-generates ID
    private int id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    //many password reset tokens can belong to one user
    @ManyToOne(fetch = FetchType.LAZY) //User object is loaded only when needed.
    @JoinColumn(name = "user_id", nullable = false)
    private User user; //used association (password reset token associated with user)

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetToken(String token, User user) {
        this.token     = token;
        this.user      = user;
        this.expiresAt = LocalDateTime.now().plusMinutes(30); // 30 min validity
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
