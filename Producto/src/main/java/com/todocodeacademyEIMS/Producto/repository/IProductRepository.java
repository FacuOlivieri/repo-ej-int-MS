package com.todocodeacademyEIMS.Producto.repository;

import com.todocodeacademyEIMS.Producto.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * FROM Product WHERE name = :name", nativeQuery = true)
    Optional<Product> findByName(@Param("name") String name);
}