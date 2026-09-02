package com.todocodeacademyEIMS.Carrito.controller;

import com.todocodeacademyEIMS.Carrito.dto.CarritoDTO;
import com.todocodeacademyEIMS.Carrito.service.ICarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/carritos")
public class CarritoController {

    @Autowired
    private ICarritoService carritoService;

    @GetMapping("/find/all")
    public ResponseEntity<List<CarritoDTO>> findAll() {
        return ResponseEntity.ok(carritoService.findAll());
    }

    @GetMapping("/find/{idCarrito}")
    public ResponseEntity<CarritoDTO> findById(@PathVariable Long idCarrito) {
        return ResponseEntity.ok(carritoService.findById(idCarrito));
    }

    @PostMapping("/save")
    public ResponseEntity<CarritoDTO> save(@RequestBody CarritoDTO carritoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.save(carritoDTO));
    }

    @PutMapping("/update/{idCarrito}")
    public ResponseEntity<CarritoDTO> update(@PathVariable Long idCarrito,
                                             @RequestBody CarritoDTO carritoDTO) {
        return ResponseEntity.ok(carritoService.update(idCarrito, carritoDTO));
    }

    @DeleteMapping("/delete/{idCarrito}")
    public ResponseEntity<Void> deleteById(@PathVariable Long idCarrito) {
        carritoService.deleteById(idCarrito);
        return ResponseEntity.noContent().build();
    }
}