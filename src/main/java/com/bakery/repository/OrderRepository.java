package com.bakery.repository;

import com.bakery.model.Order;
import com.bakery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserOrderByIdDesc(User user);
    @Query("select distinct o from Order o " +
            "left join fetch o.user " +
            "left join fetch o.items i " +
            "left join fetch i.product " +
            "order by o.id desc")
    List<Order> findAllByOrderByIdDesc();
    boolean existsByUserId(int userId);

    @Modifying
    @Query(value = "UPDATE orders SET user_id = NULL WHERE user_id = :userId", nativeQuery = true)
    void clearUserByUserId(@Param("userId") int userId);
}
