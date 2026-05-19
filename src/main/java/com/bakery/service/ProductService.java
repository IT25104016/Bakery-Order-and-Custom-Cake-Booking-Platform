package com.bakery.service;

import com.bakery.model.Product;
import com.bakery.model.ProductCategory;
import com.bakery.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findAll().stream()
                .filter(product -> product.getStock() > 0)
                .collect(Collectors.toList());
    }

    public Optional<Product> findById(int id) {
        return productRepository.findById(id);
    }

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





    public Product updateProduct(int id, String name, double price, int stock, String image) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        if (image != null && !image.isBlank()) {
            product.setImage(image);
        }
        return productRepository.save(product);
    }



    public List<Product> searchAll(String keyword) {
        String query = keyword.toLowerCase();
        return productRepository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }



    public List<Product> searchAvailable(String keyword) {
        String query = keyword.toLowerCase();
        return getAvailableProducts().stream()
                .filter(product -> product.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }


    public List<Product> getByCategory(
            ProductCategory category
    ) {

        return productRepository.findAll()
                .stream()

                .filter(product ->
                        product.getCategory()
                                == category
                )

                .filter(product ->
                        product.getStock() > 0
                )

                .collect(Collectors.toList());
    }


    public List<Product> getAllLowStock() {
        return productRepository.findAll().stream()
                .filter(product -> product.getStock() <= 5)
                .collect(Collectors.toList());
    }

    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }

    public void delete(Long id) {
        productRepository.deleteById(id.intValue());
    }

    public long count() {
        return productRepository.count();
    }
}
