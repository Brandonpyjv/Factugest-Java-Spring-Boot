package com.factugest.service;

import com.factugest.entity.Producto;
import com.factugest.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de productos: gestión del catálogo de artículos/servicios.
 *
 * Usa dos estrategias de acceso a BD según el caso:
 *   - JPA/Repository para operaciones simples (guardar, buscar por ID, eliminar).
 *     Spring Data genera el SQL automáticamente a partir de los métodos del Repository.
 *   - JdbcTemplate para la consulta de lista con JOIN a impuestos, que necesita
 *     datos de varias tablas que JPA no mapea directamente en la entidad Producto.
 */
@Service
public class ProductoService {

    @Autowired private ProductoRepository repo;

    // JdbcTemplate: acceso de bajo nivel a la BD, da control total sobre el SQL.
    // Se autowiredea automáticamente porque Spring Boot detecta el DataSource
    // en application.properties y crea el bean JdbcTemplate por nosotros.
    @Autowired private JdbcTemplate jdbc;

    /**
     * Lista todos los productos con el nombre e IVA de su impuesto asociado.
     * LEFT JOIN para que aparezcan incluso los productos sin impuesto asignado.
     * COALESCE(i.porcentaje, 0) muestra 0% si el producto es exento (cod_impuesto es null).
     */
    public List<Map<String, Object>> getAllDetailed() {
        return jdbc.queryForList("""
            SELECT p.cod_producto, p.sku, p.nombre, p.descripcion, p.precio_unitario,
                   p.stock, p.stock_minimo, p.cod_impuesto, p.unidad_medida,
                   p.codigo_barras, p.activo,
                   i.descripcion AS tax_name,
                   COALESCE(i.porcentaje, 0) AS tax_porcentaje
            FROM productos p LEFT JOIN impuestos i ON p.cod_impuesto = i.cod_impuesto
            ORDER BY p.nombre
            """);
    }

    // Los métodos siguientes delegan directamente en el repositorio JPA.
    // Spring Data JPA genera el SQL equivalente en tiempo de compilación.

    /** Devuelve todos los productos ordenados por nombre (para selects de formularios). */
    public List<Producto> getAll() { return repo.findAllByOrderByNombreAsc(); }

    /**
     * Busca un producto por PK. Devuelve Optional<Producto> (puede estar vacío)
     * obligando al que llama a manejar el caso de "no encontrado" explícitamente.
     */
    public Optional<Producto> getById(Integer id) { return repo.findById(id); }

    /**
     * Guarda (INSERT) o actualiza (UPDATE) según si el objeto tiene ID o no.
     * JPA lo detecta automáticamente: sin ID → INSERT; con ID existente → UPDATE.
     */
    public Producto save(Producto p) { return repo.save(p); }

    /** Elimina por PK. No falla si el ID no existe. */
    public void delete(Integer id) { repo.deleteById(id); }
}
