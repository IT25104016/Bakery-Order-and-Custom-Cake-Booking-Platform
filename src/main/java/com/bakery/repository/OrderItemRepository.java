package com.bakery.repository;

import com.bakery.model.OrderItem;
import com.bakery.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query("""
            SELECT oi.product.name, SUM(oi.quantity)
            FROM OrderItem oi
            WHERE oi.order.status <> :excludedStatus
            GROUP BY oi.product.id, oi.product.name
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Object[]> findTopSellingProductsExcludingStatus(@Param("excludedStatus") Order.Status excludedStatus,
                                                         Pageable pageable);
}
