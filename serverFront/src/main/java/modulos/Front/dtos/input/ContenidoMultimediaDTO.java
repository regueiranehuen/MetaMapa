package modulos.Front.dtos.input;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContenidoMultimediaDTO {
    private String url;
    private String contentType;
}
