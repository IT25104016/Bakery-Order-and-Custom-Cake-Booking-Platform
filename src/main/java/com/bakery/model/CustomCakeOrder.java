package com.bakery.model;

import java.time.LocalDate;

public class CustomCakeOrder extends CakeOrder {

    private final CustomOrder.CakeSize size;
    private final CustomOrder.CakeFlavor flavor;
    private final String customMessage;
    private final LocalDate deliveryDate;

    public CustomCakeOrder(int id, String customerName, String status,
                           CustomOrder.CakeSize size, CustomOrder.CakeFlavor flavor,
                           String customMessage, LocalDate deliveryDate) {
        super(id, customerName, status);
        this.size = size;
        this.flavor = flavor;
        this.customMessage = customMessage;
        this.deliveryDate = deliveryDate;
    }

    @Override
    public double calculatePrice() {
        return size.getBasePrice();
    }

    @Override
    public String getOrderType() {
        return "Custom Cake Order";
    }

    @Override
    public String getSummary() {
        return size.name() + " " + flavor.getLabel()
                + (customMessage != null && !customMessage.isEmpty()
                ? " | Message: \"" + customMessage + "\"" : "")
                + " | Delivery: " + deliveryDate;
    }

    public CustomOrder.CakeSize getSize() {
        return size;
    }

    public CustomOrder.CakeFlavor getFlavor() {
        return flavor;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
}
