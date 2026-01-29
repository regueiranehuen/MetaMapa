package com.tpagrupo17.Metamapa.entities.DbMain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "pais")
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", columnDefinition = "VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci")
    private String pais;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "pais_id")
    private List<Sinonimo> sinonimos;

    public Pais(){

    }
}

/*
* TABLA PAIS:
* ARGENTINA CORDOBA
* ARGENTINA CABA
* ARGENTINA SANTA FE
*
* TABLA PROVINCIA
* CORDOBA ARGENTINA
* WASHINGTON USA
* SAN LUIS ARGENTINA
* */


