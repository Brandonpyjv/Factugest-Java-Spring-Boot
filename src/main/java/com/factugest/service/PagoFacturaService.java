package com.factugest.service;

import com.factugest.entity.PagoFactura;
import com.factugest.repository.PagoFacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoFacturaService {
    @Autowired private PagoFacturaRepository repo;

    public List<PagoFactura> getAll() { return repo.findAll(); }
    public Optional<PagoFactura> getById(Integer id) { return repo.findById(id); }
    public PagoFactura save(PagoFactura p) { return repo.save(p); }
    public void delete(Integer id) { repo.deleteById(id); }
}
