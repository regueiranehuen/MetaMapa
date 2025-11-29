package modulos.Front.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modulos.Front.BodyToListConverter;
import modulos.Front.FechaParser;
import modulos.Front.GestorArchivos;
import modulos.Front.dtos.input.*;
import modulos.Front.dtos.output.AtributosModificarDTO;
import modulos.Front.dtos.output.HechosResponse;
import modulos.Front.dtos.output.UsuarioOutputDto;
import modulos.Front.dtos.output.VisualizarHechosOutputDTO;
import modulos.Front.services.HechosService;
import modulos.Front.services.SolicitudHechoService;
import modulos.Front.services.UsuarioService;
import modulos.Front.usuario.Rol;
import modulos.Front.usuario.Usuario;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/hechos")
@RequiredArgsConstructor
public class HechosController {
    private final HechosService hechosService;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SolicitudHechoService solicitudHechoService;

    @PreAuthorize("hasAnyRole('CONTRIBUYENTE', 'ADMINISTRADOR')") // Solo usuarios logueados
    @GetMapping("/mis-hechos")
    public String getHechosDelUsuario(Model model){
        System.out.println("ENTRO A GET HECHOS DEL USUARIO (Front)");

        ResponseEntity<?> rtaDto = this.hechosService.getHechosDelUsuario();

        if(rtaDto.getStatusCode().is2xxSuccessful() && rtaDto.getBody() != null){
            List<VisualizarHechosOutputDTO> hechos = BodyToListConverter.bodyToList(rtaDto, VisualizarHechosOutputDTO.class);
            model.addAttribute("listaHechos", hechos);
            return "hecho";
        }

        return "redirect:/" + rtaDto.getStatusCode().value();
    }

    @GetMapping("/public/get-all")
    public String getHechos(Model model){
        System.out.println("ENTRO A GET ALL HECHOS");
        ResponseEntity<?> rtaDto = this.hechosService.getHechos();

        if(rtaDto.getStatusCode().is2xxSuccessful() && rtaDto.getBody() != null){
            List<VisualizarHechosOutputDTO> hechos = BodyToListConverter.bodyToList(rtaDto, VisualizarHechosOutputDTO.class);
            model.addAttribute("listaHechos", hechos);

            return "hecho";
        }

        return "redirect:/" + rtaDto.getStatusCode().value();
    }


    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/subir")
    public String subirHecho(RedirectAttributes ra, @Valid @ModelAttribute SolicitudHechoInputDTO hechoInputDTO){
        System.out.println("ARCHIVO: " + hechoInputDTO.getContenidosMultimedia());

        if(!hechoInputDTO.getContenidosMultimedia().isEmpty()){
            System.out.println("SIZE LISTA DE ARCHIVOS: " + hechoInputDTO.getContenidosMultimedia().size());
        }
        ResponseEntity<?> rtaDto = this.hechosService.subirHecho(hechoInputDTO);
        if(rtaDto.getStatusCode().is2xxSuccessful()){
            return "redirect:/public/contribuir";
        }
        else if(rtaDto.getBody() != null){
            ra.addFlashAttribute(rtaDto.getBody().toString());
        }
        return "redirect:/" + rtaDto.getStatusCode().value();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/importar")
    public String importar(Model model){
        model.addAttribute("meta", new ImportacionHechosInputDTO());
        return "importarCsv";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/importar")
    public String importarHechos(@Valid @ModelAttribute ImportacionHechosInputDTO dtoInput,
                                 @RequestPart("file") MultipartFile file,
                                 RedirectAttributes ra) {

        System.out.println("ENTRE A IMPORTAR CON ESTO: " + dtoInput.getFuenteString());

        // Copiás el contenido del archivo ANTES de que termine la request
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        ByteArrayResource resource;
        try {
            byte[] bytes = file.getBytes();
            resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };
        } catch (IOException e) {
            ra.addFlashAttribute("msgError", "No se pudo leer el archivo subido.");
            return "redirect:importar";
        }

        Mono<ResponseEntity<Void>> rta = this.hechosService.importarHechos(dtoInput, resource, contentType);

        rta.subscribe(
                response -> System.out.println("Importación terminó con status: " + response.getStatusCode()),
                error -> System.err.println("Error importando CSV: " + error.getMessage())
        );

        ra.addFlashAttribute("msgExito", "El csv se está procesando, muchas gracias por su cooperación!!");
        return "redirect:importar";
    }




    @GetMapping("/public/get-mapa")
    public String getHechosConLatitudYLongitud(Model model){
        ResponseEntity<?> rtaDto = this.hechosService.getHechosConLatitudYLongitud();

        if (rtaDto.getStatusCode().is2xxSuccessful() && rtaDto.getBody() != null) {
            List<VisualizarHechosOutputDTO> hechos =
                    BodyToListConverter.bodyToList(rtaDto, VisualizarHechosOutputDTO.class);

            // Serializamos a JSON para evitar problemas de Thymeleaf con objetos complejos
            String hechosJson;
            try {
                hechosJson = objectMapper.writeValueAsString(hechos);
            } catch (Exception e) {
                hechosJson = "[]";
            }

            model.addAttribute("hechosJson", hechosJson);
            model.addAttribute("SolicitudHechoInputDTO", new SolicitudHechoInputDTO());
            return "mapa"; // nombre del template
        }
        return "redirect:/" + rtaDto.getStatusCode().value();
    }

    @GetMapping("/public/get")
    public String getHecho(Model model, Long id_hecho, String fuente,
                           @RequestParam(name = "id_solicitud", required = false) Long id_solicitud,
                           @RequestParam(name = "es-de-modificar", required = false) Boolean esDeModificar){
        ResponseEntity<?> rtaDto = this.hechosService.getHecho(id_hecho, fuente);




        System.out.println("ID DE SOLICITUD: " + id_solicitud);
        if(rtaDto.getStatusCode().is2xxSuccessful() && rtaDto.getBody() != null){
            VisualizarHechosOutputDTO hecho = (VisualizarHechosOutputDTO) rtaDto.getBody();
            model.addAttribute("hecho", hecho);
            System.out.println("CONTENIDO MULTIMEDIA: " +  hecho.getContenido());

            if (id_solicitud == null){
                model.addAttribute("esSolicitud", false);
            }
            else{
                model.addAttribute("esSolicitud", true);
            }



            model.addAttribute("puedeReportar", false);


            boolean esContribuyenteDelHecho = false;

            String usuarioActual = usuarioService.getUsernameFromSession();

            if (usuarioActual != null){

                ResponseEntity<?> rtaUsuario = usuarioService.getUsuario();

                if (rtaUsuario.getStatusCode().is2xxSuccessful() && rtaUsuario.hasBody()){

                    UsuarioOutputDto usuarioOutputDto = (UsuarioOutputDto) rtaUsuario.getBody();

                    System.out.println("FECHA SIN PARSEAR: " + hecho.getFechaAcontecimiento());
                    System.out.println("FECHA PARSEADA: " + FechaParser.parsearFecha(hecho.getFechaAcontecimiento()));


                    System.out.println("SOY UNA FUENTE FELIZ: " + hecho.getFuente());

                    if (usuarioOutputDto.getRol().equals(Rol.ADMINISTRADOR)){

                        if (id_solicitud == null){
                            HechoModificarInputDTO dtoModificar = HechoModificarInputDTO.builder()
                                    .id_hecho(hecho.getId())
                                    .titulo(hecho.getTitulo())
                                    .descripcion(hecho.getDescripcion())
                                    .latitud(hecho.getLatitud())
                                    .longitud(hecho.getLongitud())
                                    .fechaAcontecimiento(hecho.getFechaAcontecimiento())
                                    .id_pais(hecho.getId_pais())
                                    .id_provincia(hecho.getId_provincia())
                                    .id_categoria(hecho.getId_categoria())
                                    .fuente(hecho.getFuente())
                                    .contenidosMultimedia(hecho.getContenido())
                                    .build();
                            model.addAttribute("HechoModificarInputDTO", dtoModificar);
                        }

                        else if (esDeModificar){
                            System.out.println("ID SOLICITUD: " + id_solicitud);
                            ResponseEntity<?> rtaAtributosModificar = solicitudHechoService.getAtributosHechoAModificar(id_solicitud);
                            if (rtaAtributosModificar.getStatusCode().is2xxSuccessful() && rtaAtributosModificar.hasBody()){
                                AtributosModificarDTO atributosModificarDTO = (AtributosModificarDTO) rtaAtributosModificar.getBody();
                                System.out.println("TITULO A MODIFICAR: " + atributosModificarDTO.getTitulo());
                                System.out.println("CATEGORIA: " + atributosModificarDTO.getCategoria());
                                System.out.println("PAIS: " + atributosModificarDTO.getPais());

                                model.addAttribute("AtributosModificarDTO", atributosModificarDTO);
                            }
                        }
                    }



                    if (hecho.getUsername() != null){
                        esContribuyenteDelHecho = hecho.getUsername().equals(usuarioActual);
                    }

                    if (esContribuyenteDelHecho) {

                        if (usuarioOutputDto.getRol().equals(Rol.CONTRIBUYENTE)){
                            SolicitudHechoEliminarInputDTO solicitudHechoEliminarInputDTO = new SolicitudHechoEliminarInputDTO();
                            solicitudHechoEliminarInputDTO.setId_hecho(hecho.getId());
                            model.addAttribute("SolicitudHechoEliminarInputDTO", solicitudHechoEliminarInputDTO);


                            System.out.println("ID DEL HECHO: " + hecho.getId());

                            // 7 días máximo para solicitar modificar el hecho
                            if (ChronoUnit.DAYS.between(FechaParser.parsearFecha(hecho.getFechaCarga()), LocalDateTime.now()) <= 7){

                                System.out.println("FECHA: " + hecho.getFechaAcontecimiento());

                                SolicitudHechoModificarInputDTO dtoModificar = SolicitudHechoModificarInputDTO.builder()
                                        .id_hecho(hecho.getId())
                                        .titulo(hecho.getTitulo())
                                        .descripcion(hecho.getDescripcion())
                                        .latitud(hecho.getLatitud())
                                        .longitud(hecho.getLongitud())
                                        .fechaAcontecimiento(hecho.getFechaAcontecimiento())
                                        .id_pais(hecho.getId_pais())
                                        .id_provincia(hecho.getId_provincia())
                                        .id_categoria(hecho.getId_categoria())
                                        .build();

                                model.addAttribute("SolicitudHechoModificarInputDTO", dtoModificar);
                            }
                        }



                    }
                }
                else {
                    model.addAttribute("puedeReportar", false);
                }
            }
            else {
                model.addAttribute("puedeReportar", false);
            }

            return "detalleHecho";
        }

        return "redirect:/" + rtaDto.getStatusCode().value();
    }

    @PostMapping("/public/get/filtrar")
    public String getHechosFiltradosColeccion(@Valid @ModelAttribute GetHechosColeccionInputDTO inputDTO, Model model){

        System.out.println("HOLA!");

        inputDTO.setOrigenConexion(0);

        if (inputDTO.getPaisId() != null)
            System.out.println("PAISES EN GET HECHOS FILTRADOS: " + inputDTO.getPaisId());
        if (inputDTO.getProvinciaId()!=null)
            System.out.println("PROVINCIAS EN GET HECHOS FILTRADOS: " + inputDTO.getProvinciaId());

        ResponseEntity<?> rtaDto = this.hechosService.getHechosFiltradosColeccion(inputDTO);

        if(rtaDto.getStatusCode().is2xxSuccessful() && rtaDto.getBody() != null){
            List<VisualizarHechosOutputDTO> hechos = BodyToListConverter.bodyToList(rtaDto, VisualizarHechosOutputDTO.class);

            if(hechos != null) {
                for (VisualizarHechosOutputDTO hecho : hechos) {
                    System.out.println("Titulo de hecho: " + hecho.getTitulo());
                }

                model.addAttribute("listaHechos", hechos);
                return "hecho";
            }
        }
        return "redirect:/" + rtaDto.getStatusCode().value();
    }

    @PostMapping("/eliminar-hecho")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminarHecho(@RequestParam Long id, @RequestParam String fuente, RedirectAttributes ra) {
        ResponseEntity<?> rta = this.hechosService.eliminarHecho(id, fuente);

        if(rta.getStatusCode().is2xxSuccessful()){
            return "redirect:/hechos/public/get-all";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PostMapping("/modificar-hecho")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String modificarHecho(
            @ModelAttribute HechoModificarInputDTO dto,
            @RequestParam(name = "contenidosMultimediaParaAgregar", required = false)
            List<MultipartFile> archivos,
            RedirectAttributes ra) {

        System.out.println("DTO FORM: " + dto);
        System.out.println("ARCHIVOS: " + archivos);


        if (archivos != null) {
            List<ContenidoMultimediaDTO> dtos = new ArrayList<>();
            for (MultipartFile file : archivos) {
                if (!file.isEmpty()) {
                    try {
                        String ruta = GestorArchivos.guardarArchivo(file); // tu clase de antes
                        String contentType = file.getContentType();
                        dtos.add(new ContenidoMultimediaDTO(ruta, contentType));
                    } catch (IOException e) {
                        e.printStackTrace();
                        ra.addFlashAttribute("error",
                                "Error guardando archivo: " + file.getOriginalFilename());
                    }
                }
            }
            dto.setNuevasRutasMultimedia(!dtos.isEmpty() ? dtos : null);
        }




        // 3) Llamar al back: ahora el DTO ya no tiene MultipartFile en el JSON
        ResponseEntity<?> rta = hechosService.modificarHecho(dto);

        System.out.println("RESPUESTA SERVICE: " + rta);

        if (rta != null && rta.getStatusCode().is2xxSuccessful()) {
            return "redirect:/hechos/public/get-all";
        }
        if (rta != null && rta.getBody() != null) {
            ra.addFlashAttribute("error", rta.getBody().toString());
        }
        return "redirect:/500";
    }


}
