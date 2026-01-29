package com.tpagrupo17.Metamapa.entities.DbMain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sinonimo")
@Entity
public class Sinonimo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sinonimo", columnDefinition = "VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci")
    private String sinonimoStr;

    public Sinonimo(String sinonimoStr){
        this.sinonimoStr = sinonimoStr;
    }

    public Sinonimo(){

    }
}