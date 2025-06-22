package com.onedao.service;

import com.onedao.payload.ProductDto;
import com.onedao.exception.CustomException;
import com.onedao.entity.Product;
import com.onedao.entity.User;
import com.onedao.repository.ProductRepository;
import com.onedao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Product createProduct(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        // Assuming the user is fetched from the security context
        User user = userRepository.findById(1L).orElseThrow(() -> new CustomException("User  not found")); // Replace with actual user fetching logic
        product.setCreatedBy(user);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException("Product not found"));
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException("Product not found"));
        productRepository.delete(product);
    }
}
