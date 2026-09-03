# Cambios a hacer

Lista de pendientes para retomar en la otra máquina después del `pull`.
Borrar los ítems a medida que se completan.

---

## 1. Manejar el 404 de Feign en `CarritoService.addProduct`

**Estado:** pendiente

`ProductService.findByName` (microservicio Producto) lanza `ProductNotFoundException`
mapeada a HTTP 404. Feign, ante un 404, **lanza `FeignException.NotFound`**: no devuelve `null`.

Por eso el chequeo actual queda muerto:

```java
ProductDTO producto = productoAPI.findProductByName(request.getProductName());
if (producto == null) { ... }   // nunca se cumple; en su lugar sale un 500
```

**Acción (elegir una):**

- **A.** Capturar la excepción en el service y relanzar como 404 propio:

  ```java
  ProductDTO producto;
  try {
      producto = productoAPI.findProductByName(request.getProductName());
  } catch (FeignException.NotFound e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Product not found with name: " + request.getProductName());
  }
  ```

- **B.** Resolverlo con el fallback del Circuit Breaker (ver punto 2). Si se hace B,
  el bloque try/catch de A no hace falta.

---

## 2. Circuit Breaker en la llamada Feign (TODO ya marcado en el código)

**Estado:** pendiente

Dependencia ya presente en `Carrito/pom.xml`: `spring-cloud-starter-circuitbreaker-resilience4j`.

**Acción:** envolver `productoAPI.findProductByName(...)` con Resilience4j
(anotación `@CircuitBreaker` con `fallbackMethod`, o `CircuitBreakerFactory`).
Definir el comportamiento del fallback (ej. lanzar 503 / 404 según corresponda).

---

## Opcional — config Feign en `Carrito/src/main/resources/application.yaml`

Si se quieren timeouts / logs de Feign, la forma correcta es:

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          producto-service:
            connect-timeout: 5000
            read-timeout: 5000
            logger-level: full   # requiere un @Bean Logger.Level.FULL para verlo
```
