package modulos.Front.controllers;

import lombok.AllArgsConstructor;
import modulos.Front.BodyToListConverter;
import modulos.Front.dtos.output.CategoriaDTO;
import modulos.Front.dtos.input.SolicitudHechoInputDTO;
import modulos.Front.dtos.output.PaisDTO;
import modulos.Front.dtos.output.ProvinciaDTO;
import modulos.Front.services.HechosService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class HomeController {

    private final HechosService hechosService;

    @GetMapping("/")
    public String home(){
        return "index";
    }


    @GetMapping("/hechos")
    public String hechos() {
        return "hecho"; // thymeleaf busca templates/hecho.html
    }

    @GetMapping("/hecho")
    public String hecho() {
        return "hecho";
    }

    @GetMapping("/mapa")
    public String mapa() {
        return "mapa";
    }

    @GetMapping("/colecciones")
    public String colecciones() {
        return "colecciones";
    }

    /*@GetMapping("/contribuir")
    public String contribuir(@RequestParam(required = false) Long pais_id,
                             @ModelAttribute SolicitudHechoInputDTO solicitudHecho,
                             Model model) {

        ResponseEntity <?> rta = this.hechosService.getPaises();
        ResponseEntity <?> rta2 = this.hechosService.getCategorias();

        if(!rta.getStatusCode().is2xxSuccessful() || !rta2.getStatusCode().is2xxSuccessful()){
            return "redirect:/404";
        }

        List<PaisDto> paises = BodyToListConverter.bodyToList(rta, PaisDto.class);
        List<CategoriaDto> categorias = BodyToListConverter.bodyToList(rta2, CategoriaDto.class);

        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);


        if (solicitudHecho == null){
            System.out.println("soli hecho de mierda es null");
            model.addAttribute("solicitudHecho", new SolicitudHechoInputDTO());
            return "contribuir";
        }
        else{
            System.out.println("soli hecho de mierda NOOO es null");
            model.addAttribute("solicitudHecho", solicitudHecho);
            ResponseEntity <?> rta3 = this.hechosService.getProvinciasByIdPais(pais_id);
            if(!rta3.getStatusCode().is2xxSuccessful()){
                return "redirect:/404";
            }
            List<ProvinciaDto> provincias = BodyToListConverter.bodyToList(rta3, ProvinciaDto.class);
            model.addAttribute("provincias", provincias);
            return "contribuir";
        }

    }*/


    @GetMapping("/contribuir")
    public String contribuir(
            @ModelAttribute("solicitudHecho") SolicitudHechoInputDTO solicitudHecho,
            Model model) {

        // Catálogos base
        ResponseEntity<?> rtaPaises = hechosService.getPaises();
        ResponseEntity<?> rtaCategorias = hechosService.getCategorias();
        if (!rtaPaises.getStatusCode().is2xxSuccessful() || !rtaCategorias.getStatusCode().is2xxSuccessful()) {
            return "redirect:/404";
        }

        List<PaisDTO> paises = BodyToListConverter.bodyToList(rtaPaises, PaisDTO.class);
        List<CategoriaDTO> categorias = BodyToListConverter.bodyToList(rtaCategorias, CategoriaDTO.class);
        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);

        // Provincias si ya hay país seleccionado
        List<ProvinciaDTO> provincias = java.util.Collections.emptyList();
        if (solicitudHecho != null && solicitudHecho.getId_pais() != null) {
            ResponseEntity<?> rtaProv = hechosService.getProvinciasByIdPais(solicitudHecho.getId_pais());
            if (!rtaProv.getStatusCode().is2xxSuccessful()) {
                return "redirect:/404";
            }
            provincias = BodyToListConverter.bodyToList(rtaProv, ProvinciaDTO.class);
        }
        model.addAttribute("provincias", provincias);

        return "contribuir";
    }


    /*@GetMapping("/contribuir")
    public String contribuir(
            @RequestParam(required = false) Long pais_id,
            @RequestParam(required = false) Long provincia_id,
            @ModelAttribute SolicitudHechoInputDTO solicitudHecho,
            Model model) {

        model.addAttribute("solicitudHecho", solicitudHecho); // mantiene datos previos

        // Obtener países y categorías
        ResponseEntity<?> rtaPaises = hechosService.getPaises();
        ResponseEntity<?> rtaCategorias = hechosService.getCategorias();

        if (!rtaPaises.getStatusCode().is2xxSuccessful() || !rtaCategorias.getStatusCode().is2xxSuccessful()) {
            return "redirect:/404";
        }

        List<PaisDto> paises = BodyToListConverter.bodyToList(rtaPaises, PaisDto.class);
        List<CategoriaDto> categorias = BodyToListConverter.bodyToList(rtaCategorias, CategoriaDto.class);
        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);

        // Si el usuario ya seleccionó un país, cargar provincias relacionadas
        List<ProvinciaDto> provincias = List.of();
        if (pais_id != null) {
            ResponseEntity<?> rtaProvincias = hechosService.getProvinciasByIdPais(pais_id);
            if (rtaProvincias.getStatusCode().is2xxSuccessful()) {
                provincias = BodyToListConverter.bodyToList(rtaProvincias, ProvinciaDto.class);

                // También podrías guardar el país seleccionado completo si lo necesitás
                paises.stream()
                        .filter(p -> p.getId().equals(pais_id))
                        .findFirst()
                        .ifPresent(p -> model.addAttribute("pais", p));
            }
        }

        model.addAttribute("provincias", provincias);
        model.addAttribute("paisSeleccionado", pais_id);
        model.addAttribute("provinciaSeleccionada", provincia_id);

        return "contribuir";
    }
*/


    @GetMapping("/solicitudes")
    public String solicitudes() {
        return "solicitudes";
    }

    @GetMapping("/gestion")
    public String gestion() {
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
