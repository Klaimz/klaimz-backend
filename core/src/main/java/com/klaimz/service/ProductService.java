package com.klaimz.service;


import com.klaimz.model.Product;
import com.klaimz.repo.ProductRepository;
import com.klaimz.util.EntityValidators;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;

import java.util.List;

@Singleton
public class ProductService {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private EntityValidators entityValidators;


    public Product createProduct(@Valid Product product) {
        return productRepository.save(product);
    }

    public Product getProductById(String id) {
        var product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }
        return product.get();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
