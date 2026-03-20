package com.factugest.service;

import com.factugest.entity.Impuesto;
import com.factugest.repository.ImpuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImpuestoService {
    @Autowired private ImpuestoRepository repo;

    public List<Impuesto> getAll() { return repo.findAll(); }
    public Optional<Impuesto> getById(Integer id) { return repo.findById(id); }
    public Impuesto save(Impuesto i) { return repo.save(i); }
    public void delete(Integer id) { repo.deleteById(id); }
}
