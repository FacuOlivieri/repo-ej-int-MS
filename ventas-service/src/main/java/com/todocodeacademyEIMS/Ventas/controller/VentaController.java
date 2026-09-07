package com.todocodeacademyEIMS.Ventas.controller;

import com.todocodeacademyEIMS.Ventas.dto.CreateVentaRequestDTO;
import com.todocodeacademyEIMS.Ventas.dto.VentaDTO;
import com.todocodeacademyEIMS.Ventas.service.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private IVentaService ventaService;

    @GetMapping("/find/all")
    public ResponseEntity<List<VentaDTO>> findAll() {
        return ResponseEntity.ok(ventaService.findAll());
    }

    @GetMapping("/find/{idVenta}")
    public ResponseEntity<VentaDTO> findById(@PathVariable Long idVenta) {
        return ResponseEntity.ok(ventaService.findById(idVenta));
    }

    @PostMapping("/save")
    public ResponseEntity<VentaDTO> save(@RequestBody CreateVentaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.confirmarVenta(request));
    }

    @DeleteMapping("/delete/{idVenta}")
    public ResponseEntity<Void> deleteById(@PathVariable Long idVenta) {
        ventaService.deleteById(idVenta);
        return ResponseEntity.noContent().build();
    }
}
