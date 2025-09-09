package com.presta.product.services.impl;

import com.presta.product.daos.ProductRepository;
import com.presta.product.models.Product;
import com.presta.product.models.dtos.ProductDto;
import com.presta.product.services.IProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDto(product);
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        Product p = new Product();
        p.setName(productDto.name());
        p.setDescription(productDto.description());
        p.setPrice(productDto.price());
        return productRepository.save(p);
    }

    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCreatedAt(),
                product.getModifiedAt()
        );
    }
}