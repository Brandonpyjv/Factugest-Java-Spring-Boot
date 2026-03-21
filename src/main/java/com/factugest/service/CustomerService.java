package com.factugest.service;

import com.factugest.entity.Customer;
import com.factugest.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de clientes: operaciones CRUD del directorio de clientes.
 *
 * Thin service: la lógica de acceso a datos es simple (una tabla, sin JOINs complejos),
 * por lo que delega directamente en el repositorio JPA sin agregar lógica adicional.
 * Sirve como capa de indirección: si en el futuro se necesita validar el NIT
 * o sincronizar con un CRM externo, se hace aquí sin tocar el controlador.
 */
@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repo;

    /** Devuelve todos los clientes ordenados por nombre (A-Z) para listas y selects. */
    public List<Customer> getAll() { return repo.findAllByOrderByFullNameAsc(); }

    /** Busca un cliente por ID. Optional vacío si el ID no existe. */
    public Optional<Customer> getById(Integer id) { return repo.findById(id); }

    /** Guarda un cliente nuevo (INSERT) o actualiza uno existente (UPDATE) según su ID. */
    public Customer save(Customer c) { return repo.save(c); }

    /** Elimina el cliente por ID. No lanza error si no existe. */
    public void delete(Integer id) { repo.deleteById(id); }
}
