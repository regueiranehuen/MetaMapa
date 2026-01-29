package com.tpagrupo17.Metamapa.servicioEstadistica.entities;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Data
@Entity
@Table(name = "estadisticas")
public class Estadisticas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cantSolicitudesEliminacionSpam")
    private Long cantidadDeSpam;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "categoriaCantidad_id", referencedColumnName = "id")
    CategoriaCantidad categoriaCantidad;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "estadistica_id")
    List<CategoriaHora> categoriaHoras;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "estadistica_id")
    List<CategoriaProvincia> categoriaProvincias;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "estadistica_id")
    List<ColeccionProvincia> coleccionProvincias;

    // UTC
    @Column(name = "timestamp")
    private Instant timestamp;

    public Estadisticas(Long cantidadDeSpam, CategoriaCantidad categoriaCantidad, List<CategoriaHora> categoriaHoras, List<CategoriaProvincia> categoriaProvincias, List<ColeccionProvincia> coleccionProvincias) {
        this.cantidadDeSpam = cantidadDeSpam;
        this.categoriaCantidad = categoriaCantidad;
        this.categoriaHoras = categoriaHoras;
        this.categoriaProvincias = categoriaProvincias;
        this.coleccionProvincias = coleccionProvincias;
        timestamp = Instant.now();
    }

    public Estadisticas() {
        this.categoriaProvincias = new ArrayList<>();
        this.categoriaHoras = new ArrayList<>();
        this.coleccionProvincias = new ArrayList<>();
    }
}

/*
*Estadistica
*ID CANT_SPAM
*
* */
