package com.factugest.repository;

import com.factugest.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio JPA de clientes.
 *
 * Al extender JpaRepository<Customer, Integer> obtenemos gratis:
 *   findAll(), findById(), save(), deleteById(), count(), existsById()...
 *
 * Spring Data JPA interpreta el nombre de los métodos y genera el SQL:
 *   findAllByOrderByFullNameAsc → SELECT * FROM customers ORDER BY full_name ASC
 *
 * No hay que escribir ninguna implementación — Spring genera la clase en tiempo
 * de ejecución usando proxies dinámicos de Java.
 */
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Devuelve todos los clientes ordenados alfabéticamente por nombre completo.
    // Se usa en listas y selects donde importa el orden de presentación.
    List<Customer> findAllByOrderByFullNameAsc();
}
