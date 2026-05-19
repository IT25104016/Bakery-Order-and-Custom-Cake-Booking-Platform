package com.bakery.service;

import com.bakery.model.CustomOrder;
import com.bakery.model.User;
import com.bakery.repository.CustomOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;

    public CustomOrderService(CustomOrderRepository customOrderRepository) {
        this.customOrderRepository = customOrderRepository;
    }

    public CustomOrder placeCustomOrder(User user,
                                        CustomOrder.CakeSize size,
                                        CustomOrder.CakeFlavor flavor,
                                        String customMessage,
                                        LocalDate deliveryDate,
                                        String specialInstructions) {
        CustomOrder order = new CustomOrder();
        order.setUser(user);
        order.setSize(size);
        order.setFlavor(flavor);
        order.setCustomMessage(customMessage);
        order.setDeliveryDate(deliveryDate);
        order.setSpecialInstructions(specialInstructions);
        order.setStatus(CustomOrder.CustomOrderStatus.Pending);
        order.setPrice(size.getBasePrice());
        return customOrderRepository.save(order);
    }

    public List<CustomOrder> getOrdersByUser(User user) {
        return customOrderRepository.findByUserOrderByIdDesc(user);
    }

    public List<CustomOrder> getAllOrders() {
        return customOrderRepository.findAllByOrderByIdDesc();
    }

    public Optional<CustomOrder> findById(int id) {
        return customOrderRepository.findById(id);
    }

    public void updateStatus(int id, CustomOrder.CustomOrderStatus status) {
        customOrderRepository.findById(id).ifPresent(order -> {
            order.setStatus(status);
            customOrderRepository.save(order);
        });
    }
}
