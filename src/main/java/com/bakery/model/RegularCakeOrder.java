package com.bakery.model;
// INHERITANCE (OOP Concept)
// RegularCakeOrder inherits from CakeOrder
public class RegularCakeOrder extends CakeOrder {

    private final double total;  // total price of order
    private final int itemCount;  // number of cake items

      // CONSTRUCTOR===================
    public RegularCakeOrder(int id, String customerName,
                            String status, double total, int itemCount) {
          // Call parent class constructor (CakeOrder)
        super(id, customerName, status);
        // Initialize child class variables
        this.total = total;
        this.itemCount = itemCount;
    }
     // POLYMORPHISM (METHOD OVERRIDING)
    // Different implementation for Regular Order
    @Override
    public double calculatePrice() {
        return total;
    }

    @Override
    public String getOrderType() {
        return "Regular Order";   // Define order type
    }

    @Override
    public String getSummary() {
        return itemCount + " item(s) - Total: Rs. " + String.format("%.2f", calculatePrice());  // Return readable summary for UI
    }

    public int getItemCount() {
        return itemCount;
    }
}
