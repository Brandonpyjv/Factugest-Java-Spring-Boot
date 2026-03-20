package com.factugest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LogService {
    @Autowired private JdbcTemplate jdbc;

    public List<Map<String, Object>> getAll() {
        return jdbc.queryForList("""
            SELECT l.id_log, l.fecha, l.accion, l.descripcion,
                   COALESCE(u.nombre, 'Sistema') AS cod_usuario
            FROM logs l LEFT JOIN usuarios u ON l.cod_usuario = u.cod_usuario
            ORDER BY l.fecha DESC
            """);
    }
}
