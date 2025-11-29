package modulos.Front.services;

import modulos.Front.dtos.input.*;
import modulos.Front.dtos.output.ColeccionOutputDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service

public class ColeccionService {


    private final WebApiCallerService webApiCallerService;
    private String coleccionServiceUrl = "/api/coleccion";

    public ColeccionService(WebApiCallerService webApiCallerService) {
        this.webApiCallerService = webApiCallerService;
    }

    // en los methods de web api caller service que se usan acá, el primer parámetro es la url,
    // el segundo (en algunos casos) el body de json
    // el tercero el tipo de dato que retorna server back

    public ResponseEntity<?> obtenerTodasLasColecciones() {
        return webApiCallerService.getListTokenOpcional(coleccionServiceUrl + "/public/get-all", ColeccionOutputDTO.class);
    }

    public ResponseEntity<?> crearColeccion(ColeccionInputDTO inputDTO) {
        return webApiCallerService.postEntity(coleccionServiceUrl + "/crear", inputDTO, Void.class);
    }

    public ResponseEntity<?> getColeccion(Long id_coleccion) {
        return webApiCallerService.getEntityTokenOpcional(coleccionServiceUrl + "/public/get/" + id_coleccion, ColeccionOutputDTO.class);
    }

    public ResponseEntity<?> deleteColeccion(Long id_coleccion) {
        return webApiCallerService.postEntity(coleccionServiceUrl + "/delete/" + id_coleccion, Void.class);
    }

    public ResponseEntity<?> updateColeccion(ColeccionUpdateInputDTO inputDTO) {
        return webApiCallerService.postEntity(coleccionServiceUrl + "/update", inputDTO, Void.class);
    }

    public ResponseEntity<?> agregarFuente(Long id_coleccion, String dataset) {
        return webApiCallerService.postEntity(coleccionServiceUrl + "/add/fuente/?id_coleccion=" + id_coleccion + "&dataset=" + dataset, Void.class);
    }

    public ResponseEntity<?> eliminarFuente(Long id_coleccion, Long id_dataset) {
        return webApiCallerService.postEntity(coleccionServiceUrl + "/delete/fuente/?id_coleccion=" + id_coleccion + "&id_dataset=" + id_dataset, Void.class);
    }

    public ResponseEntity<?> modificarAlgoritmoConsenso(ModificarConsensoInputDTO input) {
        return webApiCallerService.postEntity(coleccionServiceUrl + "/colecciones/modificar-consenso", input, Void.class);
    }

    public ResponseEntity<?> refrescarColecciones() {
        return webApiCallerService.postEntity(coleccionServiceUrl + "/colecciones/refrescar", Void.class);
    }

    public ResponseEntity<Long> getCantColecciones() {
        return webApiCallerService.getEntityTokenOpcional(this.coleccionServiceUrl + "/public/cantColecciones", Long.class);
    }

    public ResponseEntity<List<ColeccionOutputDTO>> getColeccionesDestacadas() {
        return  webApiCallerService.getListTokenOpcional(this.coleccionServiceUrl + "/public/destacadas", ColeccionOutputDTO.class);
    }
}
