package com.tpagrupo17.Metamapa.entities.DbMain.usuario;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name="usuario")
public class Usuario {

    @Setter
    @Getter
    @Column(name = "contrasenia", length = 100)
    private String contrasenia;

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombreDeUsuario")
    private String nombreDeUsuario;

    public Usuario() {
        cantHechosSubidos = 0;
        rol = Rol.VISUALIZADOR;
        datosPersonales = new DatosPersonalesPublicador();
    }

    @Setter
    @Getter
    @Column(name = "cantHechosSubidos")
    private Integer cantHechosSubidos;

    @Getter
    @Setter
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol; // 0 visualizador, 1 contribuyente, 2 admin

    @Getter
    @Setter
    @Embedded
    private DatosPersonalesPublicador datosPersonales;

    public void incrementarHechosSubidos(){
        this.cantHechosSubidos++;
    }

    public void disminuirHechosSubidos(){
        this.cantHechosSubidos--;
    }

}