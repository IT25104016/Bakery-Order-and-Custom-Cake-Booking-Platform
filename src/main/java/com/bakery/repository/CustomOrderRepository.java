package com.bakery.repository;

import com.bakery.model.CustomOrder;
import com.bakery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomOrderRepository extends JpaRepository<CustomOrder, Integer> {
    List<CustomOrder> findByUserOrderByIdDesc(User user);
    List<CustomOrder> findAllByOrderByIdDesc();
    boolean existsByUserId(int userId);

    @Modifying
    @Query(value = "UPDATE custom_orders SET user_id = NULL WHERE user_id = :userId", nativeQuery = true)
    void clearUserByUserId(@Param("userId") int userId);
}
