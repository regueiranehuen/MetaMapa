package modulos.Front.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import modulos.Front.BodyToListConverter;
import modulos.Front.GestorArchivos;
import modulos.Front.dtos.input.*;
import modulos.Front.dtos.output.SolicitudHechoOutputDTO;
import modulos.Front.services.SolicitudHechoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/solicitudes-hecho")
@RequiredArgsConstructor
public class SolicitudHechoController {
    private final SolicitudHechoService solicitudHechoService;


    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/evaluar/subir")
    public String evaluarSolicitudSubida(@Valid @ModelAttribute SolicitudHechoEvaluarInputDTO dtoInput){
        System.out.println("JUSTIFICACION: " + dtoInput.getMensaje());
        ResponseEntity<?> rta = solicitudHechoService.evaluarSolicitudSubida(dtoInput);

        if (rta.getStatusCode().is2xxSuccessful()) {
            return "redirect:/solicitudes";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/evaluar/eliminar")
    public String evaluarSolicitudEliminacion(@Valid @ModelAttribute SolicitudHechoEvaluarInputDTO dto, RedirectAttributes ra){
        ResponseEntity<?> rta = solicitudHechoService.evaluarSolicitudEliminacion(dto);

        if(rta.getStatusCode().is2xxSuccessful()){
            return "redirect:/solicitudes";
        }
        else if(rta.getBody() != null){
            ra.addAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/evaluar/modificar")
    public String evaluarSolicitudModificacion(@Valid @ModelAttribute SolicitudHechoEvaluarInputDTO dto, RedirectAttributes ra){
        ResponseEntity<?> rta = solicitudHechoService.evaluarSolicitudModificacion(dto);

        if(rta.getStatusCode().is2xxSuccessful()){
            return "redirect:/solicitudes";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PostMapping("/public/subir-hecho")
    public String enviarSolicitudSubirHecho(@Valid @ModelAttribute SolicitudHechoInputDTO dto, RedirectAttributes ra){

        System.out.println("HOLA SOY UNA DESCRIPCION FELIZ: " + dto.getDescripcion());

        ResponseEntity<?> rta = this.solicitudHechoService.enviarSolicitudSubirHecho(dto);

        System.out.println("HOLA YA ME COMUNIQUÉ AAA");

        System.out.println("RECIBI ESTE CODIGO: " +rta.getStatusCode().value());


        if(rta.getStatusCode().is2xxSuccessful()){
            return "redirect:/public/contribuir";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONTRIBUYENTE', 'VISUALIZADOR')")
    @PostMapping("/reportes/reportar")
    public String reportar(@Valid @RequestParam Long id_hecho, @Valid @RequestParam String fuente, @Valid @RequestParam String motivo, RedirectAttributes ra){

        ResponseEntity<?> rta = solicitudHechoService.reportarHecho(motivo, id_hecho, fuente);

        if(rta.getStatusCode().is2xxSuccessful()){
            return "redirect:/hechos/get?id_usuario=" + id_hecho.toString();
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }


    @PreAuthorize("hasRole('CONTRIBUYENTE')")
    @PostMapping("/eliminar-hecho")
    public String enviarSolicitudEliminarHecho(@Valid @ModelAttribute SolicitudHechoEliminarInputDTO dto, RedirectAttributes ra){
        ResponseEntity<?> rta = this.solicitudHechoService.enviarSolicitudEliminarHecho(dto);

        if(rta.getStatusCode().is2xxSuccessful()){
            return "redirect:/hechos/public/get-all";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasRole('CONTRIBUYENTE')")
    @PostMapping("/modificar-hecho")
    public String enviarSolicitudModificarHecho(@Valid @ModelAttribute SolicitudHechoModificarInputDTO dto, RedirectAttributes ra,
                                                @RequestParam(name = "contenidosMultimediaParaAgregar", required = false)
                                                    List<MultipartFile> archivos){
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

        ResponseEntity<?> rta = this.solicitudHechoService.enviarSolicitudModificarHecho(dto);

        if(rta.getStatusCode().is2xxSuccessful()){
            return "redirect:/hechos/public/get-all";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/reportes/get/all")
    public String getAllReportes(RedirectAttributes ra) {
        ResponseEntity<?> rta = this.solicitudHechoService.getAllReportes();

        if(rta.getStatusCode().is2xxSuccessful()){
            return "reportes"; // TODO vista de lista de reportes

        }
        else if(rta.getBody() != null) {
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/reportes/evaluar")
    public String evaluarReporte(@Valid @ModelAttribute EvaluarReporteInputDTO dtoInput, RedirectAttributes ra){
        ResponseEntity<?> rta = solicitudHechoService.evaluarReporte(dtoInput);

        if (rta.getStatusCode().is2xxSuccessful()) {
            return "redirect:reportes/get/all";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    // Anda
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/get/all")
    public String getAllSolicitudes(Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = solicitudHechoService.getAllSolicitudes();

        if (rta.getStatusCode().is2xxSuccessful()) {
            List<SolicitudHechoOutputDTO> solicitudes = BodyToListConverter.bodyToList(rta, SolicitudHechoOutputDTO.class);
            model.addAttribute("solicitudes", solicitudes);
            return "solicitudes";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/get/pendientes")
    public String getSolicitudesPendientes(Model model, RedirectAttributes ra) {
        ResponseEntity<?> rta = solicitudHechoService.getSolicitudesPendientes();

        if (rta.getStatusCode().is2xxSuccessful()) {
            List<SolicitudHechoOutputDTO> solicitudes = BodyToListConverter.bodyToList(rta, SolicitudHechoOutputDTO.class);
            model.addAttribute("solicitudes", solicitudes);
            return "solicitudes";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

/*

🧩 1. Model — datos para la misma vista (sin redirect)

Usás el Model cuando estás devolviendo directamente una vista (HTML) en el mismo request.

🔹 Los datos del Model se pierden si hacés un redirect.
🔹 Se usan cuando hacés algo como return "vista" (no redirect:).

Ejemplo:

@GetMapping("/form")
public String mostrarFormulario(Model model) {
    model.addAttribute("usuario", new UsuarioDTO());
    return "formulario"; // Se muestra la vista "formulario.html"
}


👉 Se usa para renderizar datos en el mismo renderizado de la vista, típico de un GET.


    (a) addAttribute()

👉 Agrega datos como parámetros en la URL (/destino?key=value).

    @PostMapping("/procesar")
    public String procesar(RedirectAttributes ra) {
        ra.addAttribute("id", 42);
        return "redirect:/detalle"; // redirige a /detalle?id=42
    }

    (b) addFlashAttribute()

👉 Agrega datos que no van en la URL, se guardan temporalmente en sesión y se eliminan luego del redirect.

    @PostMapping("/crear")
    public String crear(@Valid FormDTO dto, RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", "Se creó correctamente");
        ra.addFlashAttribute("tipo", "success");
        return "redirect:/form";
    }
    */
}
