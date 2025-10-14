package modulos.Front.services;

import org.springframework.stereotype.Service;

@Service
public class HechosService {
    private final WebApiCallerService webApiCallerService;
    private String coleccionServiceUrl = "http://localhost:8080/api/coleccion";

    public HechosService(WebApiCallerService webApiCallerService){
        this.webApiCallerService = webApiCallerService;
    }


}