package modulos.Front;

import lombok.Data;

@Data
public class ContenidoMultimedia {


    private Long id;


    private String url;

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
