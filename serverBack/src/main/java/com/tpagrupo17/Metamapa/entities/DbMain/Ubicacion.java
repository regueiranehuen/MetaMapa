package com.tpagrupo17.Metamapa.entities.DbMain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "ubicacion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pais_provincia",     // nombre de la constraint en DB
                        columnNames = {"pais_id", "provincia_id"}
                )
        }
)
public class Ubicacion {
    public Ubicacion(Pais pais, Provincia provincia) {
        this.pais = pais;
        this.provincia = provincia;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provincia_id")
    private Provincia provincia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pais_id")
    private Pais pais;

    public Ubicacion() {

    }
}