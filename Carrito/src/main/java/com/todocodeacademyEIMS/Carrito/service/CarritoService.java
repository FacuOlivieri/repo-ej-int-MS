package com.todocodeacademyEIMS.Carrito.service;

import com.todocodeacademyEIMS.Carrito.dto.AddProductRequestDTO;
import com.todocodeacademyEIMS.Carrito.dto.CarritoDTO;
import com.todocodeacademyEIMS.Carrito.dto.ProductDTO;
import com.todocodeacademyEIMS.Carrito.dto.ProductItemDTO;
import com.todocodeacademyEIMS.Carrito.mapper.Mapper;
import com.todocodeacademyEIMS.Carrito.model.Carrito;
import com.todocodeacademyEIMS.Carrito.model.ProductItem;
import com.todocodeacademyEIMS.Carrito.repository.ICarritoRepository;
import com.todocodeacademyEIMS.Carrito.repository.ProductoAPI;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                .map(this::enrichProductData)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarritoDTO findById(Long idCarrito) {
        return enrichProductData(Mapper.mapToDTO(findEntityById(idCarrito)));
    }

    @Override
    @Transactional
    public CarritoDTO save(CarritoDTO carritoDTO) {
        Carrito carrito = Mapper.mapFromDTO(carritoDTO);
        carrito.setIdCarrito(null);
        carrito.getProductList().forEach(productItem -> productItem.setIdProductLine(null));

        recalculate(carrito);

        return enrichProductData(Mapper.mapToDTO(carritoRepository.save(carrito)));
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

        return enrichProductData(Mapper.mapToDTO(carritoRepository.save(carrito)));
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

        int quantity = request.getQuantity();
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than 0");
        }

        Carrito carrito = findEntityById(idCarrito);

        ProductDTO producto = findProductByName(request.getProductName());

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
        return enrichProductData(Mapper.mapToDTO(carritoRepository.save(carrito)));
    }

    /**
     * Closes the FEIGN SEAM for the cart read path.
     *
     * By design carritodb never stores product name or brand: a {@link ProductItem}
     * only keeps idProduct plus the unitPrice snapshot. Those descriptive fields are
     * resolved here, on read, against producto-service so the response carries them
     * without duplicating product data in this service's database.
     *
     * For every line the existing {@link ProductDTO} instance is mutated in place:
     * only name and brand are filled, the unitPrice snapshot stays exactly as carrito
     * stored it. A missing product (deleted in producto-service) is tolerated per line
     * so one gap does not break the whole cart response.
     *
     * TODO: producto-service has no bulk-by-ids endpoint, so findAll() does N per-line
     * calls. Add a bulk endpoint later, and the Circuit Breaker from task A2 should wrap
     * findProductById too.
     */
    private CarritoDTO enrichProductData(CarritoDTO carritoDTO) {
        if (carritoDTO == null || carritoDTO.getProductList() == null) {
            return carritoDTO;
        }

        for (ProductItemDTO productItemDTO : carritoDTO.getProductList()) {
            if (productItemDTO == null) {
                continue;
            }
            ProductDTO product = productItemDTO.getProduct();
            if (product == null || product.getIdProduct() == null) {
                continue;
            }
            try {
                ProductDTO resolved = productoAPI.findProductById(product.getIdProduct());
                product.setName(resolved.getName());
                product.setBrand(resolved.getBrand());
            } catch (FeignException.NotFound e) {
                // Product deleted in producto-service; leave name/brand null for this line, continue.
            }
        }

        return carritoDTO;
    }

    /**
     * Resuelve un producto por nombre contra el microservicio Producto.
     *
     * Producto responde 404 cuando el nombre no existe, y ante un 404 Feign lanza
     * {@link FeignException.NotFound}: nunca devuelve null. Sin este catch la excepción
     * sube sin mapear y el cliente recibe un 500 en lugar de un 404.
     *
     * TODO: envolver esta llamada con un Circuit Breaker (Resilience4j). Cuando se haga,
     * el fallback debe ignorar NotFound: un producto inexistente es un error del cliente,
     * no una caída del servicio.
     */
    private ProductDTO findProductByName(String productName) {
        try {
            return productoAPI.findProductByName(productName);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Product not found with name: " + productName);
        }
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
