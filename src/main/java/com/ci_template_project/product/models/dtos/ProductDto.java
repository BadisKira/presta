package com.ci_template_project.product.models.dtos;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {}
