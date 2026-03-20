package com.factugest.service;

import com.factugest.entity.Descuento;
import com.factugest.repository.DescuentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DescuentoService {
    @Autowired private DescuentoRepository repo;

    public List<Descuento> getAll() { return repo.findAll(); }
    public Optional<Descuento> getById(Integer id) { return repo.findById(id); }
    public Descuento save(Descuento d) { return repo.save(d); }
    public void delete(Integer id) { repo.deleteById(id); }
}
