package com.presta.product.services;



import com.presta.product.models.Product;
import com.presta.product.models.dtos.ProductDto;

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


    Product createProduct(ProductDto productDto);
}