//ordering ability - customer dashboard - customer welcome message

package com.bakery.model; //represent database entities
//IPMOE
import jakarta.persistence.*; //database mapping
import lombok.Data; //automatically generates getters and setters
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor; //

@Entity
@DiscriminatorValue("CUSTOMER")
@Data //lombok 
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
    
public class CustomerUser extends User {

    // Polymorphism
    @Override
    public String getRole() {
        return "CUSTOMER"; //Returns customer role
    }

    @Override
    public String getDashboardUrl() {
        return "/customer/products"; //Customer dashboard path
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, " + getName() + "! Explore our fresh bakes!";
    }

    // Customer specific methods 
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
