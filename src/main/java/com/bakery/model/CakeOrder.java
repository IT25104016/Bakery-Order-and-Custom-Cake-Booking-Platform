package com.bakery.model;

public abstract class CakeOrder {

    protected int id;
    protected String customerName;
    protected String status;

    public CakeOrder(int id, String customerName, String status) {
        this.id = id;
        this.customerName = customerName;
        this.status = status;
    }

    public abstract double calculatePrice();
    public abstract String getOrderType();
    public abstract String getSummary();

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderLabel() {
        return "[" + getOrderType() + " #" + id + "] " + customerName
                + " - Rs. " + String.format("%.2f", calculatePrice());
    }
}
