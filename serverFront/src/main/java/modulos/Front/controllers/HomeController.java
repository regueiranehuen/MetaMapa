package modulos.Front.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import modulos.Front.BodyToListConverter;
import modulos.Front.dtos.input.ColeccionInputDTO;
import modulos.Front.dtos.input.SolicitudHechoEvaluarInputDTO;
import modulos.Front.dtos.output.*;
import modulos.Front.dtos.input.SolicitudHechoInputDTO;
import modulos.Front.services.ColeccionService;
import modulos.Front.services.HechosService;
import modulos.Front.services.SolicitudHechoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
public class HomeController {

    private final HechosService hechosService;
    private final ColeccionService coleccionService;
    private final SolicitudHechoService solicitudHechoService;

    @GetMapping("/")
    public String home(Model model) {
        ResponseEntity<Long> statsHechos = hechosService.getCantHechos();
        ResponseEntity<Long> statsColecciones = coleccionService.getCantColecciones();
        ResponseEntity<Integer> statsSolicitudes = solicitudHechoService.getPorcentajeSolicitudesProcesadas();

        model.addAttribute("statsHechos", statsHechos.getBody());
        model.addAttribute("statsColecciones", statsColecciones.getBody());
        model.addAttribute("statsSolicitudes", statsSolicitudes.getBody());

        return "index";
    }

    @GetMapping("/public/mapa")
    public String mapa() {
        return "mapa";
    }


    @GetMapping("/public/contribuir")
    public String contribuir(
            @ModelAttribute("solicitudHecho") SolicitudHechoInputDTO solicitudHecho,
            Model model, HttpSession httpSession) {

        // Catálogos base
        ResponseEntity<?> rtaPaises = hechosService.getPaises();
        ResponseEntity<?> rtaCategorias = hechosService.getCategorias();
        if (!rtaPaises.getStatusCode().is2xxSuccessful() || !rtaCategorias.getStatusCode().is2xxSuccessful()) {
            return "redirect:/404";
        }

        List<PaisDto> paises = BodyToListConverter.bodyToList(rtaPaises, PaisDto.class);
        List<CategoriaDto> categorias = BodyToListConverter.bodyToList(rtaCategorias, CategoriaDto.class);
        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);

        Double latitud = solicitudHecho.getLatitud();
        Double longitud = solicitudHecho.getLongitud();

        if (latitud != null & longitud != null){
            ResponseEntity<?> rtaLatLon = hechosService.getPaisYProvincia(latitud, longitud);
            if (rtaLatLon.hasBody()){
                PaisProvinciaDTO paisProvinciaDTO = (PaisProvinciaDTO) rtaLatLon.getBody();
                model.addAttribute("pais", paisProvinciaDTO.getPaisDto());
                model.addAttribute("provincia", paisProvinciaDTO.getProvinciaDto());
            }
        }

        // Provincias si ya hay país seleccionado
        List<ProvinciaDto> provincias = java.util.Collections.emptyList();
        if (solicitudHecho.getId_pais() != null) {
            ResponseEntity<?> rtaProv = hechosService.getProvinciasByIdPais(solicitudHecho.getId_pais());
            if (!rtaProv.getStatusCode().is2xxSuccessful()) {
                return "redirect:/404";
            }
            provincias = BodyToListConverter.bodyToList(rtaProv, ProvinciaDto.class);
        }
        model.addAttribute("provincias", provincias);

        return "contribuir";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/solicitudes")
    public String solicitudes(Model model, RedirectAttributes ra) {
        ResponseEntity<?> rta = solicitudHechoService.getSolicitudesPendientes();

        System.out.println(rta.getBody());

        if (rta.getStatusCode().is2xxSuccessful()) {
            List<SolicitudHechoOutputDTO> solicitudes = BodyToListConverter.bodyToList(rta, SolicitudHechoOutputDTO.class);
            model.addAttribute("solicitudes", solicitudes);
            model.addAttribute("solicitudHechoEvaluarInputDTO", new SolicitudHechoEvaluarInputDTO());
            return "solicitudes";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }

        return "solicitudes";
    }

    @GetMapping("/gestion")
    public String gestion(Model model) {
        model.addAttribute("coleccionForm", new ColeccionInputDTO());
        return "gestion";
    }



    @GetMapping("/404")
    public String notFound(){
        return "404";
    }

    @GetMapping("/500")
    public String internalServerError(){
        return "500";
    }

    @GetMapping("/403")
    public String accessDenied(){
        return "403";
    }
}
