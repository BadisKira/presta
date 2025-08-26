package com.ci_template_project.product.service;

import com.ci_template_project.product.daos.ProductRepository;
import com.ci_template_project.product.models.Product;
import com.ci_template_project.product.models.dtos.ProductDto;
import com.ci_template_project.product.services.impl.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        product = new Product("Laptop", "Gaming laptop", BigDecimal.valueOf(1200.00));
        product.setId(1L);
        product.setCreatedAt(LocalDateTime.now());
        product.setModifiedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        List<ProductDto> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).name());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_Found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDto dto = productService.getProductById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("Laptop", dto.name());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(99L);
        });

        assertEquals("Product not found with id: 99", exception.getMessage());
        verify(productRepository, times(1)).findById(99L);
    }
}
