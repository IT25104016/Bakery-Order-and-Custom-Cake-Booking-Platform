package com.bakery.service;

import com.bakery.model.Product;
import com.bakery.model.ProductRating;
import com.bakery.model.User;
import com.bakery.repository.ProductRatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductRatingService {

    private final ProductRatingRepository ratingRepository;

    public ProductRatingService(ProductRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    // Submit or update a rating
    public ProductRating submitRating(User user, Product product, int stars, String review) {
        Optional<ProductRating> existing = ratingRepository.findByProductAndUser(product, user);
        ProductRating rating = existing.orElse(new ProductRating());
        rating.setUser(user);
        rating.setProduct(product);
        rating.setStars(Math.max(1, Math.min(5, stars))); // clamp 1-5
        rating.setReview(review != null && !review.isBlank() ? review.trim() : null);
        return ratingRepository.save(rating);
    }


    @Transactional
    public void deleteRating(
            int userId,
            int productId
    ) {

        ratingRepository
                .findByUserIdAndProductId(
                        userId,
                        productId
                )
                .ifPresent(ratingRepository::delete);
    }

    public List<ProductRating> getRatingsForProduct(Product product) {
        return ratingRepository.findByProductOrderByIdDesc(product);
    }

    public Optional<ProductRating> getUserRatingForProduct(User user, Product product) {
        return ratingRepository.findByProductAndUser(product, user);
    }

    // Returns average stars (0.0 if no ratings)
    public double getAverageStars(Product product) {
        Double avg = ratingRepository.findAverageStarsByProduct(product);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public long getRatingCount(Product product) {
        return ratingRepository.countByProduct(product);
    }

    // Build a map of productId -> average stars for a list of products (efficient batch)
    public Map<Integer, Double> getAverageStarsMap(List<Product> products) {
        return products.stream().collect(Collectors.toMap(
                Product::getId,
                this::getAverageStars
        ));
    }

    public Map<Integer, Long> getRatingCountMap(List<Product> products) {
        return products.stream().collect(Collectors.toMap(
                Product::getId,
                this::getRatingCount
        ));
    }
}
