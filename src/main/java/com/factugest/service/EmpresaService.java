package com.factugest.service;

import com.factugest.entity.Empresa;
import com.factugest.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de empresas: gestión de las empresas emisoras de facturas.
 *
 * En un sistema multi-empresa, cada factura emitida lleva el NIT y datos
 * de la empresa que la emite. Este servicio provee esa lista al formulario
 * de nueva factura para que el usuario elija cuál empresa emite.
 */
@Service
public class EmpresaService {

    @Autowired private EmpresaRepository repo;

    /** Lista todas las empresas registradas (para el select del formulario de facturas). */
    public List<Empresa> getAll() { return repo.findAll(); }

    public Optional<Empresa> getById(Integer id) { return repo.findById(id); }

    /** Guarda o actualiza una empresa. Spring Data detecta si tiene ID para UPDATE o INSERT. */
    public Empresa save(Empresa e) { return repo.save(e); }

    public void delete(Integer id) { repo.deleteById(id); }
}
