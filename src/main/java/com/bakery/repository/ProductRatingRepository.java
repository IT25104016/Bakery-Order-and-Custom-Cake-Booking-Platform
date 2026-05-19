package com.bakery.repository;

import com.bakery.model.Product;
import com.bakery.model.ProductRating;
import com.bakery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRatingRepository extends JpaRepository<ProductRating, Integer> {

    List<ProductRating> findByProductOrderByIdDesc(Product product);

    Optional<ProductRating> findByProductAndUser(Product product, User user);

    @Query("SELECT AVG(r.stars) FROM ProductRating r WHERE r.product = :product")
    Double findAverageStarsByProduct(@Param("product") Product product);

    long countByProduct(Product product);

    Optional<ProductRating> findByUserIdAndProductId(
            int userId,
            int productId
    );

    @Modifying
    @Query("DELETE FROM ProductRating r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") int userId);
}
