package com.factugest.service;

import com.factugest.entity.MetodoPago;
import com.factugest.repository.MetodoPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetodoPagoService {
    @Autowired private MetodoPagoRepository repo;

    public List<MetodoPago> getAll() { return repo.findAll(); }
    public Optional<MetodoPago> getById(Integer id) { return repo.findById(id); }
    public MetodoPago save(MetodoPago m) { return repo.save(m); }
    public void delete(Integer id) { repo.deleteById(id); }
}
