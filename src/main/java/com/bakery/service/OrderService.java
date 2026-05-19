package com.bakery.service;

import com.bakery.model.CartItem;
import com.bakery.model.Order;
import com.bakery.model.OrderItem;
import com.bakery.model.Product;
import com.bakery.model.User;
import com.bakery.repository.OrderItemRepository;
import com.bakery.repository.OrderRepository;
import com.bakery.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    public static class TopSellingItem {
        private final String productName;
        private final long quantity;
        private final int percentage;

        public TopSellingItem(String productName, long quantity, int percentage) {
            this.productName = productName;
            this.quantity = quantity;
            this.percentage = percentage;
        }

        public String getProductName() {
            return productName;
        }

        public long getQuantity() {
            return quantity;
        }

        public int getPercentage() {
            return percentage;
        }
    }

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order placeOrderWithProducts(User user,
                                        List<CartItem> cartItems,
                                        Map<Integer, Product> productMap,
                                        Order.PaymentMethod paymentMethod,
                                        String deliveryAddress) {
        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            if (product == null) {
                throw new IllegalStateException("Product not found: " + cartItem.getProductName());
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                        "\"" + product.getName() + "\" stock is low! Available: "
                                + product.getStock() + ", Requested: " + cartItem.getQuantity()
                );
            }
        }

        double total = cartItems.stream()
                .mapToDouble(CartItem::getTotal)
                .sum();

        Order order = new Order();
        order.setUser(user);
        order.setTotal(total);
        order.setStatus(Order.Status.Pending);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : Order.PaymentMethod.CashOnDelivery);
        order.setDeliveryAddress(
                deliveryAddress != null && !deliveryAddress.isBlank() ? deliveryAddress.trim() : null
        );

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());

            item.setPrice(cartItem.getPrice());
            orderItemRepository.save(item);
        }

        return savedOrder;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByIdDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    public Optional<Order> findById(int id) {
        return orderRepository.findById(id);
    }

    public void updateStatus(int orderId, Order.Status status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }

    public void updateStatus(Long orderId, String status) {
        updateStatus(orderId.intValue(), Order.Status.valueOf(status));
    }

    public long count() {
        return orderRepository.count();
    }

    public long pendingOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == Order.Status.Pending)
                .count();
    }

    public double totalRevenue() {
        return orderRepository.findAll().stream()
                .mapToDouble(Order::getTotal)
                .sum();
    }

    public List<TopSellingItem> topSellingItems(int limit) {
        List<Object[]> rows = orderItemRepository.findTopSellingProducts(PageRequest.of(0, limit));
        long maxQuantity = rows.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .max()
                .orElse(0);

        return rows.stream()
                .map(row -> {
                    String productName = row[0] != null ? row[0].toString() : "Deleted Product";
                    long quantity = ((Number) row[1]).longValue();
                    int percentage = maxQuantity > 0
                            ? Math.max(6, (int) Math.round(quantity * 100.0 / maxQuantity))
                            : 0;

                    return new TopSellingItem(productName, quantity, percentage);
                })
                .toList();
    }
}
