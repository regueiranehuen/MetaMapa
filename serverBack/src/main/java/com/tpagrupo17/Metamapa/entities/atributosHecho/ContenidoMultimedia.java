package com.tpagrupo17.Metamapa.entities.atributosHecho;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ContenidoMultimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url")
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoContenido tipo;

    public void almacenarTipoDeArchivo(String mimeType){
        if (mimeType == null) {
            tipo = TipoContenido.INVALIDO;
        }

        if (mimeType.startsWith("image/")) {
            tipo = TipoContenido.IMAGEN;
        } else if (mimeType.startsWith("video/")) {
            tipo = TipoContenido.VIDEO;
        } else if (mimeType.startsWith("audio/")) {
            tipo =  TipoContenido.AUDIO;
        } else if (mimeType.startsWith("text/") || mimeType.equals("application/json") || mimeType.equals("application/xml")) {
            tipo =  TipoContenido.TEXTO;
        } else {
            tipo =  TipoContenido.INVALIDO;
        }
    }

}
