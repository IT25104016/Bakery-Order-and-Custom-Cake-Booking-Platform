package com.bakery.model;

public class RegularCakeOrder extends CakeOrder {

    private final double total;
    private final int itemCount;

    public RegularCakeOrder(int id, String customerName,
                            String status, double total, int itemCount) {
        super(id, customerName, status);
        this.total = total;
        this.itemCount = itemCount;
    }

    @Override
    public double calculatePrice() {
        return total;
    }

    @Override
    public String getOrderType() {
        return "Regular Order";
    }

    @Override
    public String getSummary() {
        return itemCount + " item(s) - Total: Rs. " + String.format("%.2f", calculatePrice());
    }

    public int getItemCount() {
        return itemCount;
    }
}
