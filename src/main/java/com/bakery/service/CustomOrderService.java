package com.bakery.service;

import com.bakery.model.CustomOrder;
import com.bakery.model.User;

import com.bakery.repository.CustomOrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

//Marks this class as a service layer component
@Service
public class CustomOrderService {

    //repositary objects reference
    private final CustomOrderRepository customOrderRepository;

    //constructor (parameters are received from the spring)
    public CustomOrderService(CustomOrderRepository customOrderRepository) {
        this.customOrderRepository = customOrderRepository;
    }

    //places a custom order
    public CustomOrder placeCustomOrder(User user,
                                        CustomOrder.CakeSize size,
                                        CustomOrder.CakeFlavor flavor,
                                        String customMessage,
                                        LocalDate deliveryDate,
                                        String specialInstructions) {

        //creates a new custom order object
        CustomOrder order = new CustomOrder();

        //sets the values to the order object
        order.setUser(user);
        order.setSize(size);
        order.setFlavor(flavor);

        order.setCustomMessage(customMessage);

        order.setDeliveryDate(deliveryDate);

        order.setSpecialInstructions(specialInstructions);

        order.setStatus(CustomOrder.CustomOrderStatus.Pending);

        //gets the base price from the selected cake size
        order.setPrice(size.getBasePrice());

        //saves the order in the database and returns it
        return customOrderRepository.save(order);
    }

    //gets all custom orders of a specific user
    public List<CustomOrder> getOrdersByUser(User user) {

        //finds orders by user and sorts by latest order first
        return customOrderRepository.findByUserOrderByIdDesc(user);
    }

    //gets all custom orders
    public List<CustomOrder> getAllOrders() {

        //returns all orders sorted in descending order
        return customOrderRepository.findAllByOrderByIdDesc();
    }

    //finds a custom order using id
    public Optional<CustomOrder> findById(int id) {

        //Optional is used because the order may or may not exist
        return customOrderRepository.findById(id);
    }

    //updates the status of the custom order
    public void updateStatus(int id, CustomOrder.CustomOrderStatus status) {

        //if the order exists update the status
        customOrderRepository.findById(id).ifPresent(order -> {

            order.setStatus(status);

            //save updated order
            customOrderRepository.save(order);
        });
    }

    //@Transactional keeps all database operations in one transaction
    @Transactional
    public void cancelOrderForUser(int id, User user) {

        //finds the order using the id
        CustomOrder order = customOrderRepository.findById(id)

                //throws error if order is not found
                .orElseThrow(() ->
                        new IllegalStateException("Custom order not found."));

        //checks whether the logged in user owns the order
        if (order.getUser() == null || user == null ||
                order.getUser().getId() != user.getId()) {

            //throws error if another user tries to cancel
            throw new IllegalStateException(
                    "You can only cancel your own custom orders.");
        }

        //checks whether the order can still be cancelled
        if (!canCustomerCancel(order)) {

            //throws error if order status does not allow cancellation
            throw new IllegalStateException(
                    "This custom order can no longer be cancelled.");
        }

        //changes the status to cancelled
        order.setStatus(CustomOrder.CustomOrderStatus.Cancelled);

        //saves updated order
        customOrderRepository.save(order);
    }

    //checks whether the customer can cancel the order
    public boolean canCustomerCancel(CustomOrder order) {

        return order != null //checks order object exists

                && order.getStatus() != null //checks status exists

                //calls the method from enum to check cancellable statuses
                && order.getStatus().isCustomerCancellable();
    }
}
