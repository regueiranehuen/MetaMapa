package raiz.Front.services;

import raiz.Front.dtos.input.SinonimoInputDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SinonimoService {

    private String url_base = "/api/sinonimos";
    private final WebApiCallerService  webApiCallerService;

    public SinonimoService(WebApiCallerService webApiCallerService) {
        this.webApiCallerService = webApiCallerService;
    }

    public ResponseEntity<Void> crearSinonimoCategoria(SinonimoInputDTO sinonimoInputDTO) {
        return webApiCallerService.postEntity(this.url_base + "/crear/categoria", sinonimoInputDTO, Void.class);
    }
    public ResponseEntity<Void> crearSinonimoPais(SinonimoInputDTO sinonimoInputDTO) {
        return webApiCallerService.postEntity(this.url_base + "/crear/pais", sinonimoInputDTO, Void.class);
    }
    public ResponseEntity<Void> crearSinonimoProvincia(SinonimoInputDTO sinonimoInputDTO) {
        return webApiCallerService.postEntity(this.url_base + "/crear/provincia",  sinonimoInputDTO, Void.class);
    }

}
