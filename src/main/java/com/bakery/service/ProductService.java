package com.bakery.service;

import com.bakery.model.Product;
import com.bakery.model.ProductCategory;
import com.bakery.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// SERVICE LAYER
// OOP CONCEPT: ENCAPSULATION + ABSTRACTION
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
     // GET ALL PRODUCTS
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
     // GET AVAILABLE PRODUCTS (STOCK > 0)
    public List<Product> getAvailableProducts() {
        return productRepository.findAll().stream()
                .filter(product -> product.getStock() > 0)
                .collect(Collectors.toList());
    }

    public Optional<Product> findById(int id) {
        return productRepository.findById(id);
    }
     // SAVE NEW PRODUCT=============================
    public Product saveProduct(

            String name,

            double price,

            int stock,

            ProductCategory category,

            String image

    ) {

        Product product =
                new Product();

        product.setName(name);

        product.setPrice(price);

        product.setStock(stock);

        product.setCategory(category);

        product.setImage(
                image != null && !image.isBlank()
                        ? image
                        : "default.svg"
        );

        return productRepository.save(product);
    }




     // UPDATE PRODUCT
    public Product updateProduct(int id, String name, double price, int stock, String image) {
        Product product = productRepository.findById(id).orElseThrow();
          // Update values
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        if (image != null && !image.isBlank()) {
            product.setImage(image);
        }
        return productRepository.save(product);
    }


       // SEARCH PRODUCTS (ALL)
    public List<Product> searchAll(String keyword) {
        String query = keyword.toLowerCase();
        return productRepository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }


     // SEARCH ONLY AVAILABLE PRODUCTS
    public List<Product> searchAvailable(String keyword) {
        String query = keyword.toLowerCase();
        return getAvailableProducts().stream()
                .filter(product -> product.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }

      // FILTER BY CATEGORY + AVAILABLE STOCK
    public List<Product> getByCategory(
            ProductCategory category
    ) {

        return productRepository.findAll()
                .stream()
                 // filter by category
                .filter(product ->
                        product.getCategory()
                                == category
                )
                   // filter by stock availability
                .filter(product ->
                        product.getStock() > 0
                )

                .collect(Collectors.toList());
    }

     // LOW STOCK PRODUCTS (<= 5)
    public List<Product> getAllLowStock() {
        return productRepository.findAll().stream()
                .filter(product -> product.getStock() <= 5)
                .collect(Collectors.toList());
    }
      // DELETE PRODUCT
    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }

    public void delete(Long id) {
        productRepository.deleteById(id.intValue());
    }
      // COUNT PRODUCTS
    public long count() {
        return productRepository.count();
    }
}
