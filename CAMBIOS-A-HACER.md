# Cambios a hacer

Lista de pendientes del ejercicio integrador.
Borrar los ítems a medida que se completan.

---

## Estado actual

| Servicio | Puerto | Estado |
|---|---|---|
| `config-server` | 8888 | listo (sirve `config-data` desde Git) |
| `eureka-server` | 8761 | listo |
| `Producto` (`producto-service`) | 8083 | CRUD completo + `/products/search?productName=` |
| `Carrito` (`carrito-service`) | 8082 | CRUD completo + `addProduct` vía Feign a Producto |
| `Ventas` (`ventas-service`) | 8084 (a definir) | **esqueleto vacío**: solo `VentasApplication` y un `application.yaml` con el nombre |
| API Gateway | 8080 | **no existe** |

Decisiones ya tomadas para Ventas:

- La venta guarda un **snapshot** de las líneas del carrito (queda inmutable).
- **No** se maneja stock: `Product` sigue siendo `id / name / brand / unitPrice`.
- El Gateway lo implementa Facu por su cuenta (ver bloque C).

---

# BLOQUE A — Deuda pendiente en Carrito

## A1. Manejar el 404 de Feign en `CarritoService.addProduct`

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

- **B.** Resolverlo con el fallback del Circuit Breaker (ver A2). Si se hace B,
  el bloque try/catch de A no hace falta.

> Ojo: si se va por B, el fallback **no debe** convertir un 404 en 503. Un producto
> inexistente es un error del cliente, no una caída del servicio. Lo prolijo es
> `ignoreExceptions = FeignException.NotFound.class` en el circuit breaker.

---

## A2. Circuit Breaker en la llamada Feign (TODO ya marcado en el código)

**Estado:** pendiente

Dependencia ya presente en `Carrito/pom.xml`: `spring-cloud-starter-circuitbreaker-resilience4j`.

**Acción:** envolver `productoAPI.findProductByName(...)` con Resilience4j
(anotación `@CircuitBreaker` con `fallbackMethod`, o `CircuitBreakerFactory`).
Definir el comportamiento del fallback (ej. 503 si Producto está caído, 404 si no existe).

Notas:

- Hay **dos** comentarios `// TODO: envolver esta llamada con un Circuit Breaker` en
  `CarritoService.addProduct`; el primero (arriba de la validación de `quantity`) está
  fuera de lugar — borrarlo.
- El `fallbackMethod` tiene que tener **la misma firma** que el método protegido más un
  parámetro `Throwable` al final.

---

## A3. Opcional — config Feign en `Carrito/src/main/resources/application.yaml`

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

---

# BLOQUE B — Microservicio Ventas (el grueso del trabajo)

`Ventas/pom.xml` **ya tiene todas las dependencias necesarias** (webmvc, jpa, mysql, lombok,
eureka-client, openfeign, loadbalancer, resilience4j, actuator). No hay que tocar el pom.

Flujo objetivo:

```
POST /ventas/save { "idCarrito": 1 }
   -> ventas-service pide GET /carritos/find/1 a carrito-service (Feign + Eureka)
   -> copia las líneas del carrito a la venta (snapshot)
   -> recalcula el total del lado del servidor
   -> guarda la Venta en ventasdb
   -> devuelve 201 con la VentaDTO
```

## B1. Configuración base

**Estado:** pendiente

`Ventas/src/main/resources/application.yaml` — hoy solo tiene el `spring.application.name`.
Completar siguiendo el molde de Carrito:

```yaml
spring:
  application:
    name: ventas-service      # renombrar: hoy dice "Ventas"

  datasource:
    url: jdbc:mysql://localhost:3306/ventasdb?createDatabaseIfNotExist=true&serverTimezone=UTC
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: 8084

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

Y en `VentasApplication`: agregar `@EnableDiscoveryClient` y `@EnableFeignClients`
(igual que `CarritoApplication`).

## B2. Modelo (`model/`)

**Estado:** pendiente

```java
// Venta
Long idVenta;              // @Id @GeneratedValue IDENTITY
Long idCarrito;            // referencia lógica al carrito de origen (otra DB, NO relación JPA)
LocalDateTime fechaVenta;
@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
List<VentaItem> items;
double total;
```

```java
// VentaItem  -> snapshot de una línea del carrito
Long idVentaLine;          // @Id @GeneratedValue IDENTITY
Long idProduct;            // id del producto en productodb (sin relación JPA)
int quantity;
double unitPrice;          // precio congelado al momento de la venta
double subtotal;
@ManyToOne @JoinColumn(name = "idVenta")
Venta venta;
```

Mismo criterio que en Carrito: los ids de otras bases se guardan como `Long` planos,
nunca como relación JPA.

## B3. DTOs (`dto/`)

**Estado:** pendiente

Propios de Ventas:

- `VentaDTO` — `idVenta`, `idCarrito`, `fechaVenta`, `List<VentaItemDTO> items`, `total`
- `VentaItemDTO` — `idVentaLine`, `idProduct`, `quantity`, `unitPrice`, `subtotal`
- `CreateVentaRequestDTO` — `idCarrito`

Copias para deserializar lo que responde `carrito-service` (Feign necesita las clases del
lado del cliente; se duplican a propósito, cada microservicio es autónomo):

- `CarritoDTO` — `idCarrito`, `List<ProductItemDTO> productList`, `totalPrice`
- `ProductItemDTO` — `idProductLine`, `ProductDTO product`, `quantity`, `subtotal`
- `ProductDTO` — `idProduct`, `name`, `brand`, `unitPrice`

> Recordatorio: el `Mapper` de Carrito hoy devuelve `name` y `brand` en **null**
> (comentario "FEIGN SEAM"). A Ventas no le molesta porque solo usa `idProduct`,
> `unitPrice`, `quantity` y `subtotal`. Si en algún momento se quiere el nombre del
> producto dentro de la venta, hay que completar ese mapper primero.

## B4. Repositorios (`repository/`)

**Estado:** pendiente

- `IVentaRepository extends JpaRepository<Venta, Long>`
  (los `VentaItem` se persisten y borran por cascada, igual que `ProductItem` en Carrito
  — no hace falta repositorio propio).
- `CarritoAPI` — cliente Feign:

  ```java
  @FeignClient(name = "carrito-service")
  public interface CarritoAPI {
      @GetMapping("/carritos/find/{idCarrito}")
      CarritoDTO findCarritoById(@PathVariable Long idCarrito);
  }
  ```

## B5. Mapper (`mapper/Mapper.java`)

**Estado:** pendiente

- `Venta -> VentaDTO` y `VentaItem -> VentaItemDTO`
- `CarritoDTO -> List<VentaItem>` (la conversión clave: de la respuesta de Carrito a las
  líneas de la venta), acordándose de setear la back-reference `item.setVenta(venta)`;
  sin eso la FK `idVenta` queda en null.

## B6. Service (`service/IVentaService` + `VentaService`)

**Estado:** pendiente

Métodos:

- `List<VentaDTO> findAll()`
- `VentaDTO findById(Long idVenta)` — 404 si no existe
- `VentaDTO confirmarVenta(CreateVentaRequestDTO request)` — el método principal
- `void deleteById(Long idVenta)` — 404 si no existe

Lógica de `confirmarVenta`:

1. Traer el carrito por Feign. Si tira `FeignException.NotFound` -> 404 propio.
2. Si el carrito viene sin líneas -> 400 "No se puede vender un carrito vacío".
3. Copiar cada línea a un `VentaItem` (snapshot de `unitPrice`).
4. **Recalcular subtotales y total del lado del servidor**, igual que hace
   `CarritoService.recalculate`. Nunca confiar en el `totalPrice` que llega por HTTP.
5. `fechaVenta = LocalDateTime.now()`.
6. Guardar y devolver la `VentaDTO`.

`@Transactional` en los métodos de escritura, `@Transactional(readOnly = true)` en las
lecturas — mismo criterio que `CarritoService`.

## B7. Controller (`controller/VentaController.java`)

**Estado:** pendiente

`@RequestMapping("/ventas")`, respetando el estilo de los otros dos controllers:

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/ventas/find/all` | 200 `List<VentaDTO>` |
| GET | `/ventas/find/{idVenta}` | 200 `VentaDTO` |
| POST | `/ventas/save` | 201 `VentaDTO` (body: `CreateVentaRequestDTO`) |
| DELETE | `/ventas/delete/{idVenta}` | 204 |

No se expone `update`: una venta confirmada no se edita. Si se quisiera anular, va como
cambio de estado (ver B9), no como `PUT` genérico.

## B8. Circuit Breaker en `confirmarVenta`

**Estado:** pendiente

Mismo tratamiento que A2, pero sobre `carritoAPI.findCarritoById(...)`:
`@CircuitBreaker(name = "carritoService", fallbackMethod = "...")` con
`ignoreExceptions = FeignException.NotFound.class`, para no confundir "carrito inexistente"
con "carrito-service caído".

## B9. Decisiones abiertas de Ventas (definir antes de codear el service)

**Estado:** pendiente de decisión

1. **¿Qué pasa con el carrito después de vender?**
   - (a) No se toca — el carrito queda igual. Es lo más simple y lo más seguro.
   - (b) Se borra / se vacía llamando por Feign a `DELETE /carritos/delete/{id}`.
     Problema clásico de sistemas distribuidos: si la venta ya se guardó y el borrado falla,
     queda inconsistente. Si se hace, dejar el borrado **después** del `save` y tolerar el
     fallo (log y seguir), no romper la venta por eso.
2. **¿Se puede vender dos veces el mismo carrito?** Si no, hace falta un
   `existsByIdCarrito` en el repositorio y devolver 409 Conflict.
3. **¿Hace falta un campo `cliente`?** El ejercicio no lo pide y no hay microservicio de
   usuarios. Si se agrega, que sea un `String` simple, no otra llamada distribuida.

---

# BLOQUE C — API Gateway (lo hace Facu)

**Estado:** pendiente

Módulo nuevo `gateway` en el puerto 8080 con `spring-cloud-starter-gateway` +
`eureka-client`, ruteando por `lb://` a los tres servicios:

- `/products/**`  -> `lb://producto-service`
- `/carritos/**`  -> `lb://carrito-service`
- `/ventas/**`    -> `lb://ventas-service`

A tener en cuenta cuando se arme:

- Spring Cloud Gateway es **reactivo** (WebFlux): el módulo no debe tener
  `spring-boot-starter-web` / `webmvc` o no arranca (salvo que se use la variante
  `spring-cloud-starter-gateway-mvc`).
- Con `spring.cloud.gateway.discovery.locator.enabled: true` las rutas salen solas desde
  Eureka, pero conviene definirlas a mano para tener control de los paths.
- Arrancarlo al final, cuando Ventas ya responda por su puerto.

---

# BLOQUE D — Limpiezas menores

**Estado:** pendiente

1. **`ProductService.findByName`** — variable `incomingName` declarada y nunca usada, y el
   `.toLowerCase()` se le aplica al parámetro de un query nativo `WHERE name = :name`.
   En MySQL con collation por defecto (`..._ci`) funciona igual porque la comparación ya es
   case-insensitive, pero el código miente sobre lo que hace. Limpiar: o se saca el
   `toLowerCase()`, o se usa `LOWER(name) = :name` en el query.
2. **`CarritoService`** — imports sin usar (`FeignClient`, `GetMapping`, `PathVariable`) y
   el TODO duplicado mencionado en A2.
3. **`ProductoAPI` (Carrito)** — importa `CarritoDTO` y no lo usa.
4. **`config-data`** — solo tiene `eureka-server.yaml`. Si la idea es que el config-server
   sirva de verdad la configuración, faltan `producto-service.yaml`, `carrito-service.yaml`
   y `ventas-service.yaml` ahí, y que cada microservicio los consuma
   (`spring.config.import: optional:configserver:http://localhost:8888`). Hoy cada servicio
   tiene su config local y el config-server queda decorativo.
5. **`.idea/compiler.xml`** aparece modificado en git — decidir si se ignora
   (agregarlo a `.gitignore`) o se commitea.

---

# Orden sugerido

1. A1 + A2 — cerrar Carrito. Es poco código y deja probado el patrón Feign + Circuit Breaker
   que después se replica igual en Ventas.
2. B1 — config de Ventas; verificar que levanta y se registra en Eureka antes de escribir
   una sola clase de dominio.
3. B9 (decidir) y después B2 -> B3 -> B4 -> B5 -> B6 -> B7: Ventas de punta a punta.
4. B8 — circuit breaker en Ventas, replicando lo de A2.
5. C — gateway.
6. D — limpiezas, antes de la entrega.
