package modulos.Front.services;

import modulos.Front.dtos.output.CategoriaDTO;
import modulos.Front.dtos.output.PaisDTO;
import modulos.Front.dtos.input.GetHechosColeccionInputDTO;
import modulos.Front.dtos.input.ImportacionHechosInputDTO;
import modulos.Front.dtos.input.SolicitudHechoInputDTO;
import modulos.Front.dtos.output.ProvinciaDTO;
import modulos.Front.dtos.output.VisualizarHechosOutputDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HechosService {
    private final WebApiCallerService webApiCallerService;
    private String hechoServiceUrl = "/api/hechos";

    public HechosService(WebApiCallerService webApiCallerService){
        this.webApiCallerService = webApiCallerService;
    }


    public ResponseEntity<?> getHecho(Long id_hecho, String fuente) {
        return webApiCallerService.getEntitySinToken(this.hechoServiceUrl + "/public/get?id_hecho=" + id_hecho + "&fuente=" + fuente, VisualizarHechosOutputDTO.class);
    }

    public ResponseEntity<?> subirHecho(SolicitudHechoInputDTO hechoInputDTO) {
        return webApiCallerService.postEntity(this.hechoServiceUrl + "/subir", hechoInputDTO, Void.class);
    }

    public ResponseEntity<?> importarHechos(ImportacionHechosInputDTO dtoInput, MultipartFile file) {
        return webApiCallerService.importarHecho(file, dtoInput);
    }

    public ResponseEntity<?> getHechos() {
        return webApiCallerService.getListSinToken(this.hechoServiceUrl + "/public/get-all?origen=0", VisualizarHechosOutputDTO.class);
    }

    public ResponseEntity<?> getHechosFiltradosColeccion(GetHechosColeccionInputDTO inputDTO) {
        return webApiCallerService.postList(this.hechoServiceUrl + "/public/get/filtrar", inputDTO, GetHechosColeccionInputDTO.class);
    }

    public ResponseEntity<?> getPaises() {
        return webApiCallerService.getListSinToken(this.hechoServiceUrl + "/public/paises/get-all", PaisDTO.class);
    }

    public ResponseEntity<?> getProvinciasByIdPais(Long id_pais) {
        return webApiCallerService.getListSinToken(this.hechoServiceUrl + "/public/provincias?id_pais="+id_pais, ProvinciaDTO.class);
    }

    public ResponseEntity<?> getCategorias() {
        return webApiCallerService.getListSinToken(this.hechoServiceUrl + "/public/categorias/get-all", CategoriaDTO.class);
    }
}
