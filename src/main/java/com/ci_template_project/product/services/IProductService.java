package com.ci_template_project.product.services;



import com.ci_template_project.product.models.dtos.ProductDto;

import java.util.List;

public interface IProductService {

    /**
     * Récupère tous les produits
     *
     * @return liste de ProductDto
     */
    List<ProductDto> getAllProducts();

    /**
     * Récupère un produit par son ID
     *
     * @param id l'identifiant du produit
     * @return ProductDto correspondant
     * @throws RuntimeException si le produit n'existe pas
     */
    ProductDto getProductById(Long id);
}