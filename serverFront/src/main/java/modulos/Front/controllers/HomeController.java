package modulos.Front.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import modulos.Front.BodyToListConverter;
import modulos.Front.ContenidoMultimedia;
import modulos.Front.dtos.input.*;
import modulos.Front.dtos.output.*;
import modulos.Front.services.ColeccionService;
import modulos.Front.services.HechosService;
import modulos.Front.services.SolicitudHechoService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
        ResponseEntity<List<ColeccionOutputDTO>> coleccionesDestacadas = coleccionService.getColeccionesDestacadas();
        ResponseEntity<List<VisualizarHechosOutputDTO>> hechosDestacados = hechosService.getHechosDestacados();

        model.addAttribute("statsHechos", statsHechos.getBody());
        model.addAttribute("statsColecciones", statsColecciones.getBody());
        model.addAttribute("statsSolicitudes", statsSolicitudes.getBody());
        model.addAttribute("coleccionesDestacadas", coleccionesDestacadas.getBody());
        model.addAttribute("hechosDestacados", hechosDestacados.getBody());

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

    @PreAuthorize("hasRole('CONTRIBUYENTE')")
    @PostMapping("/solicitud-modificacion")
    public String solicitudModificacion(@Valid @ModelAttribute SolicitudHechoModificarInputDTO dto, Model model){
        // Catálogos base
        System.out.println("FECHA ACONTECIMIENTO: " + dto.getFechaAcontecimiento());
        ResponseEntity<?> rtaPaises = hechosService.getPaises();
        ResponseEntity<?> rtaCategorias = hechosService.getCategorias();
        if (!rtaPaises.getStatusCode().is2xxSuccessful() || !rtaCategorias.getStatusCode().is2xxSuccessful()) {
            return "redirect:/404";
        }

        List<PaisDto> paises = BodyToListConverter.bodyToList(rtaPaises, PaisDto.class);
        List<CategoriaDto> categorias = BodyToListConverter.bodyToList(rtaCategorias, CategoriaDto.class);
        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);

        Double latitud = dto.getLatitud();
        Double longitud = dto.getLongitud();

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
        if (dto.getId_pais() != null) {
            ResponseEntity<?> rtaProv = hechosService.getProvinciasByIdPais(dto.getId_pais());
            if (!rtaProv.getStatusCode().is2xxSuccessful()) {
                return "redirect:/404";
            }
            provincias = BodyToListConverter.bodyToList(rtaProv, ProvinciaDto.class);
        }
        model.addAttribute("provincias", provincias);

        String fecha = dto.getFechaAcontecimiento();

        if (fecha != null && fecha.contains("T")) {
            fecha = fecha.substring(0, fecha.indexOf("T"));
        }

        dto.setFechaAcontecimiento(fecha);

        ResponseEntity<?> rta = hechosService.getContenidoMultimediaHecho(dto.getId_hecho(), "DINAMICA");

        if (rta.getStatusCode().is2xxSuccessful() && rta.hasBody()){
            List<ContenidoMultimedia> contenidoMultimedia = BodyToListConverter.bodyToList(rta, ContenidoMultimedia.class);
            dto.setContenidosMultimedia(contenidoMultimedia);
        }

        model.addAttribute("camposViejos", dto);

        return "modificar";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/modificacion-hecho")
    public String modificarHecho(@Valid @ModelAttribute HechoModificarInputDTO dto, Model model){
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

        Double latitud = dto.getLatitud();
        Double longitud = dto.getLongitud();

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
        if (dto.getId_pais() != null) {
            ResponseEntity<?> rtaProv = hechosService.getProvinciasByIdPais(dto.getId_pais());
            if (!rtaProv.getStatusCode().is2xxSuccessful()) {
                return "redirect:/404";
            }
            provincias = BodyToListConverter.bodyToList(rtaProv, ProvinciaDto.class);
        }
        model.addAttribute("provincias", provincias);


        String fecha = dto.getFechaAcontecimiento();

        if (fecha != null && fecha.contains("T")) {
            fecha = fecha.substring(0, fecha.indexOf("T"));
        }

        dto.setFechaAcontecimiento(fecha);

        model.addAttribute("camposViejos", dto);

        ResponseEntity<?> rta = hechosService.getContenidoMultimediaHecho(dto.getId_hecho(), dto.getFuente());

        if (rta.getStatusCode().is2xxSuccessful() && rta.hasBody()){

            List<ContenidoMultimedia> contenidoMultimedia = BodyToListConverter.bodyToList(rta, ContenidoMultimedia.class);
            System.out.println("ENCONTRE CONTENIDO MULTIMEDIA JAA: " + contenidoMultimedia);
            dto.setContenidosMultimedia(contenidoMultimedia);
        }

        return "modificar";
    }

    @PreAuthorize("hasRole('CONTRIBUYENTE')")
    @GetMapping("/modificar-hecho-cont")
    public String mostrarFormModificarContribuyente(@ModelAttribute("camposViejos") SolicitudHechoModificarInputDTO dto,
                                                    Model model) {

        // --- MISMA LÓGICA QUE EN solicitudModificacion ---
        ResponseEntity<?> rtaPaises = hechosService.getPaises();
        ResponseEntity<?> rtaCategorias = hechosService.getCategorias();
        if (!rtaPaises.getStatusCode().is2xxSuccessful() || !rtaCategorias.getStatusCode().is2xxSuccessful()) {
            return "redirect:/404";
        }

        List<PaisDto> paises = BodyToListConverter.bodyToList(rtaPaises, PaisDto.class);
        List<CategoriaDto> categorias = BodyToListConverter.bodyToList(rtaCategorias, CategoriaDto.class);
        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);

        Double latitud = dto.getLatitud();
        Double longitud = dto.getLongitud();

        if (latitud != null && longitud != null){
            ResponseEntity<?> rtaLatLon = hechosService.getPaisYProvincia(latitud, longitud);
            if (rtaLatLon.hasBody()){
                PaisProvinciaDTO paisProvinciaDTO = (PaisProvinciaDTO) rtaLatLon.getBody();
                model.addAttribute("pais", paisProvinciaDTO.getPaisDto());
                model.addAttribute("provincia", paisProvinciaDTO.getProvinciaDto());
            }
        }

        List<ProvinciaDto> provincias = java.util.Collections.emptyList();
        if (dto.getId_pais() != null) {
            ResponseEntity<?> rtaProv = hechosService.getProvinciasByIdPais(dto.getId_pais());
            if (!rtaProv.getStatusCode().is2xxSuccessful()) {
                return "redirect:/404";
            }
            provincias = BodyToListConverter.bodyToList(rtaProv, ProvinciaDto.class);
        }
        model.addAttribute("provincias", provincias);

        String fecha = dto.getFechaAcontecimiento();
        if (fecha != null && fecha.contains("T")) {
            fecha = fecha.substring(0, fecha.indexOf("T"));
        }
        dto.setFechaAcontecimiento(fecha);

        ResponseEntity<?> rta = hechosService.getContenidoMultimediaHecho(dto.getId_hecho(), "DINAMICA");

        if (rta.getStatusCode().is2xxSuccessful() && rta.hasBody()){

            List<ContenidoMultimedia> contenidoMultimedia = BodyToListConverter.bodyToList(rta, ContenidoMultimedia.class);
            System.out.println("ENCONTRE CONTENIDO MULTIMEDIA JAA: " + contenidoMultimedia);
            dto.setContenidosMultimedia(contenidoMultimedia);
        }

        model.addAttribute("camposViejos", dto);

        return "modificar";
    }


    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/modificar-hecho-adm")
    public String mostrarFormModificarAdmin(@ModelAttribute("camposViejos") HechoModificarInputDTO dto,
                                            Model model) {

        ResponseEntity<?> rtaPaises = hechosService.getPaises();
        ResponseEntity<?> rtaCategorias = hechosService.getCategorias();
        if (!rtaPaises.getStatusCode().is2xxSuccessful() || !rtaCategorias.getStatusCode().is2xxSuccessful()) {
            return "redirect:/404";
        }

        List<PaisDto> paises = BodyToListConverter.bodyToList(rtaPaises, PaisDto.class);
        List<CategoriaDto> categorias = BodyToListConverter.bodyToList(rtaCategorias, CategoriaDto.class);
        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);

        Double latitud = dto.getLatitud();
        Double longitud = dto.getLongitud();

        if (latitud != null && longitud != null){
            ResponseEntity<?> rtaLatLon = hechosService.getPaisYProvincia(latitud, longitud);
            if (rtaLatLon.hasBody()){
                PaisProvinciaDTO paisProvinciaDTO = (PaisProvinciaDTO) rtaLatLon.getBody();
                model.addAttribute("pais", paisProvinciaDTO.getPaisDto());
                model.addAttribute("provincia", paisProvinciaDTO.getProvinciaDto());
            }
        }

        List<ProvinciaDto> provincias = java.util.Collections.emptyList();
        if (dto.getId_pais() != null) {
            ResponseEntity<?> rtaProv = hechosService.getProvinciasByIdPais(dto.getId_pais());
            if (!rtaProv.getStatusCode().is2xxSuccessful()) {
                return "redirect:/404";
            }
            provincias = BodyToListConverter.bodyToList(rtaProv, ProvinciaDto.class);
        }
        model.addAttribute("provincias", provincias);

        String fecha = dto.getFechaAcontecimiento();
        if (fecha != null && fecha.contains("T")) {
            fecha = fecha.substring(0, fecha.indexOf("T"));
        }
        dto.setFechaAcontecimiento(fecha);


        ResponseEntity<?> rta = hechosService.getContenidoMultimediaHecho(dto.getId_hecho(), dto.getFuente());

        if (rta.getStatusCode().is2xxSuccessful() && rta.hasBody()){

            List<ContenidoMultimedia> contenidoMultimedia = BodyToListConverter.bodyToList(rta, ContenidoMultimedia.class);
            System.out.println("ENCONTRE CONTENIDO MULTIMEDIA JAA: " + contenidoMultimedia);
            dto.setContenidosMultimedia(contenidoMultimedia);
        }

        model.addAttribute("camposViejos", dto);

        return "modificar";
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

    @GetMapping("/400")
    public String badRequest(){
        return "400";
    }

    @GetMapping("/403")
    public String accessDenied(){
        return "403";
    }

    @GetMapping("/404")
    public String notFound(){
        return "404";
    }

    @GetMapping("/409")
    public String conflict(){
        return "409";
    }

    @GetMapping("/500")
    public String internalServerError(){
        return "500";
    }


}
