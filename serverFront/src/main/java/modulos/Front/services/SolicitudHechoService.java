package modulos.Front.services;

import org.springframework.stereotype.Service;

@Service
public class SolicitudHechoService {
    private final WebApiCallerService webApiCallerService;
    private String coleccionServiceUrl = "http://localhost:8080/api/coleccion";

    public SolicitudHechoService(WebApiCallerService webApiCallerService){
        this.webApiCallerService = webApiCallerService;
    }
}