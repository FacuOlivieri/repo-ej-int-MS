package com.todocodeacademyEIMS.Carrito.service;

import com.todocodeacademyEIMS.Carrito.dto.AddProductRequestDTO;
import com.todocodeacademyEIMS.Carrito.dto.CarritoDTO;
import com.todocodeacademyEIMS.Carrito.dto.ProductDTO;
import com.todocodeacademyEIMS.Carrito.mapper.Mapper;
import com.todocodeacademyEIMS.Carrito.model.Carrito;
import com.todocodeacademyEIMS.Carrito.model.ProductItem;
import com.todocodeacademyEIMS.Carrito.repository.ICarritoRepository;
import com.todocodeacademyEIMS.Carrito.repository.ProductoAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CarritoService implements ICarritoService {

    @Autowired
    private ICarritoRepository carritoRepository;

    @Autowired
    private ProductoAPI productoAPI;

    @Override
    @Transactional(readOnly = true)
    public List<CarritoDTO> findAll() {
        return carritoRepository.findAll()
                .stream()
                .map(Mapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarritoDTO findById(Long idCarrito) {
        return Mapper.mapToDTO(findEntityById(idCarrito));
    }

    @Override
    @Transactional
    public CarritoDTO save(CarritoDTO carritoDTO) {
        Carrito carrito = Mapper.mapFromDTO(carritoDTO);
        carrito.setIdCarrito(null);
        carrito.getProductList().forEach(productItem -> productItem.setIdProductLine(null));

        recalculate(carrito);

        return Mapper.mapToDTO(carritoRepository.save(carrito));
    }

    @Override
    @Transactional
    public CarritoDTO update(Long idCarrito, CarritoDTO carritoDTO) {
        Carrito carrito = findEntityById(idCarrito);
        Carrito incoming = Mapper.mapFromDTO(carritoDTO);

        // The managed collection is cleared and refilled instead of replaced:
        // orphanRemoval only deletes the old rows if Hibernate keeps tracking the same list.
        carrito.getProductList().clear();
        for (ProductItem productItem : incoming.getProductList()) {
            productItem.setIdProductLine(null);
            productItem.setCarrito(carrito);
            carrito.getProductList().add(productItem);
        }

        recalculate(carrito);

        return Mapper.mapToDTO(carritoRepository.save(carrito));
    }

    @Override
    @Transactional
    public void deleteById(Long idCarrito) {
        if (!carritoRepository.existsById(idCarrito)) {
            throw notFound(idCarrito);
        }
        carritoRepository.deleteById(idCarrito);
    }

    @Override
    @Transactional
    public CarritoDTO addProduct(Long idCarrito, AddProductRequestDTO request) {

        // TODO: envolver esta llamada con un Circuit Breaker (Resilience4j).
        int quantity = request.getQuantity();
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than 0");
        }

        Carrito carrito = findEntityById(idCarrito);



        // TODO: envolver esta llamada con un Circuit Breaker (Resilience4j).
        ProductDTO producto = productoAPI.findProductByName(request.getProductName());
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Product not found with name: " + request.getProductName());
        }

        ProductItem item = carrito.getProductList().stream()
                .filter(productItem -> producto.getIdProduct().equals(productItem.getIdProduct()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            addToCart(carrito, producto, quantity);
        } else {
            incrementQuantity(item, producto, quantity);
        }

        recalculate(carrito);
        return Mapper.mapToDTO(carritoRepository.save(carrito));
    }

    /**
     * Agrega una línea nueva al carrito con el precio traído de Producto.
     */
    private void addToCart(Carrito carrito, ProductDTO producto, int quantity) {
        ProductItem item = ProductItem.builder()
                .idProduct(producto.getIdProduct())
                .quantity(quantity)
                .unitPrice(producto.getUnitPrice())
                .carrito(carrito)
                .build();
        carrito.getProductList().add(item);
    }

    /**
     * Suma unidades a una línea existente y refresca el snapshot de precio.
     */
    private void incrementQuantity(ProductItem item, ProductDTO producto, int quantity) {
        item.setQuantity(item.getQuantity() + quantity);
        item.setUnitPrice(producto.getUnitPrice());
    }


    /**
     * Recalculates every subtotal and the cart total on the server.
     * Amounts are never taken from the request: a client could send unitPrice = 0.01.
     *
     * FEIGN SEAM: this is where the Producto microservice will be queried by idProduct
     * to fetch the real unit price and to validate that the product exists, instead of
     * trusting the unitPrice that arrives in the DTO.
     */
    private void recalculate(Carrito carrito) {
        double totalPrice = 0d;

        for (ProductItem productItem : carrito.getProductList()) {
            productItem.setSubtotal(productItem.getUnitPrice() * productItem.getQuantity());
            totalPrice += productItem.getSubtotal();
        }

        carrito.setTotalPrice(totalPrice);
    }

    private Carrito findEntityById(Long idCarrito) {
        return carritoRepository.findById(idCarrito)
                .orElseThrow(() -> notFound(idCarrito));
    }

    private ResponseStatusException notFound(Long idCarrito) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito not found with id: " + idCarrito);
    }





}
