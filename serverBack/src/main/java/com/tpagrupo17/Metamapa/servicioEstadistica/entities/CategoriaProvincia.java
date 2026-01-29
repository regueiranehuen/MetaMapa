package com.tpagrupo17.Metamapa.servicioEstadistica.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "categoria_provincia")
@AllArgsConstructor
public class CategoriaProvincia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria_id")
    private Long categoria_id;
    @Column(name = "provincia_id")
    private Long provincia_id;
    @Column(name = "cantidad_hechos")
    private Long cantidad;

    @Column(name = "timestamp")
    private Instant timestamp;

    public CategoriaProvincia(Long categoria_id, Long provincia_id, Long cantidad) {
        this.categoria_id = categoria_id;
        this.provincia_id = provincia_id;
        this.cantidad = cantidad;
        this.timestamp = Instant.now();
    }

    public CategoriaProvincia() {

    }
}
