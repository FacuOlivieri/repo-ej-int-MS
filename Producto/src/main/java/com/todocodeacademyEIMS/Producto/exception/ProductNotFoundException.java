package com.todocodeacademyEIMS.Producto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a product cannot be found by its identifier or name.
 * Mapped to HTTP 404 so callers get a proper "Not Found" response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long idProduct) {
        super("Product not found with id: " + idProduct);
    }

    public ProductNotFoundException(String name) {
        super("Product not found with name: " + name);
    }
}