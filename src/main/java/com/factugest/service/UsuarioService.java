package com.factugest.service;

import com.factugest.entity.Usuario;
import com.factugest.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de usuarios del sistema.
 *
 * IMPORTANTE: el hashing de contraseñas NO se hace aquí — se delega a
 * PasswordMigrationRunner (al arrancar) y al controlador de usuarios que
 * debe llamar a passwordEncoder.encode() antes de llamar a save().
 * Este servicio trabaja con los objetos Usuario tal como llegan.
 */
@Service
public class UsuarioService {

    @Autowired private UsuarioRepository repo;

    public List<Usuario> getAll()                  { return repo.findAll(); }
    public Optional<Usuario> getById(Integer id)   { return repo.findById(id); }
    public Usuario save(Usuario u)                 { return repo.save(u); }
    public void delete(Integer id)                 { repo.deleteById(id); }
}
