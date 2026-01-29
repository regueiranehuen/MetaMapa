package com.tpagrupo17.Metamapa.entities.DbMain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.tpagrupo17.Metamapa.entities.DbMain.usuario.Usuario;

@Getter
@Setter
@Entity
@Table (name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "solicitud_hecho_id")
    Long solicitud_hecho_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_usuario_id"))
    Usuario receptor;

    @Column (name = "texto")
    String textoMensaje;

    public Mensaje(Long solicitud_hecho_id, Usuario receptor, String textoMensaje){
        this.solicitud_hecho_id=solicitud_hecho_id;
        this.receptor=receptor;
        this.textoMensaje=textoMensaje;
    }

    public Mensaje() {

    }
}