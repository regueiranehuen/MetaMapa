package com.tpagrupo17.Metamapa.entities.DbEstatica;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dataset")
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fuente", nullable = false)
    private String fuente;

    @Transient
    private String storagePath;

    protected Dataset() { }

    public Dataset(String fuente) {
        this.fuente = fuente;
    }
}