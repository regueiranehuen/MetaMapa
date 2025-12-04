package modulos.Front.services;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoriaService {
    private final WebApiCallerService webApiCallerService;
    private final String categoriasUrl = "/api/categoria";


    public ResponseEntity<?> crearCategoria(String categoria) {
        return webApiCallerService.postEntity(this.categoriasUrl + "/add?categoria=" + categoria, Void.class);
    }
    public ResponseEntity<?> addSinonimo(String sinonimo, Long id_categoria) {
        return webApiCallerService.postEntity(this.categoriasUrl + "/add/sinonimo/categoria?id_categoria=" + id_categoria + "&sinonimo=" + sinonimo, Void.class);
    }

}
