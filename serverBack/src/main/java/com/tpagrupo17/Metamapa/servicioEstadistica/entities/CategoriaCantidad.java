package com.tpagrupo17.Metamapa.servicioEstadistica.entities;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "categoria_cantidad")
@AllArgsConstructor
public class CategoriaCantidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria_id")
    private Long categoria_id;

    @Column(name = "cantidad_hechos")
    private Integer cantidad;

    @Column(name = "timestamp")
    private Instant timestamp;

    public CategoriaCantidad(Long categoria_id, Integer cantidad) {
        this.categoria_id = categoria_id;
        this.cantidad = cantidad;
        this.timestamp = Instant.now();
    }


    public CategoriaCantidad() {

    }
}
