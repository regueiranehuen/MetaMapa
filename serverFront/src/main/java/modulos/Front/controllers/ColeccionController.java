package modulos.Front.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modulos.Front.BodyToListConverter;
import modulos.Front.dtos.input.ColeccionInputDTO;
import modulos.Front.dtos.input.ModificarConsensoInputDTO;
import modulos.Front.dtos.input.RefrescarColeccionesInputDTO;
import modulos.Front.dtos.output.ColeccionOutputDTO;
import modulos.Front.services.ColeccionService;
import org.apache.catalina.Authenticator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/colecciones")
@RequiredArgsConstructor
public class ColeccionController {

    private final ColeccionService coleccionService;

    // Prueba de conexión entre el server front y el server back
    /*@GetMapping("/get-all")
    public ResponseEntity<?> obtenerTodasLasColecciones(){
        return coleccionService.obtenerTodasLasColecciones();
    }*/

    @PostMapping("/crear")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String crearColeccion(@Valid @ModelAttribute ColeccionInputDTO inputDTO,
                                 Model model,
                                 RedirectAttributes ra) {

        ResponseEntity<?> rta = coleccionService.crearColeccion(inputDTO);

        if (rta.getStatusCode().is2xxSuccessful()) {
            ra.addFlashAttribute("mensaje", "Se creó correctamente la colección");
            ra.addFlashAttribute("tipo", "success");
            // TODO revisar redirecciones
            return "redirect:/crear";
        }
        // TODO una única vista cambiando el nro de error o una vista x cada error?
        return "redirect:/" + rta.getStatusCode().value();
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAnyRole('VISUALIZADOR', 'CONTRIBUYENTE', 'ADMINISTRADOR')")
    public String obtenerTodasLasColecciones(Model model){
        ResponseEntity<?> rta = coleccionService.obtenerTodasLasColecciones();

        if (rta.getStatusCode().is2xxSuccessful() && rta.getBody() != null) {
            List<ColeccionOutputDTO> colecciones = BodyToListConverter.bodyToList(rta, ColeccionOutputDTO.class);
            model.addAttribute("colecciones", colecciones);
            model.addAttribute("titulo", "Listado de colecciones");
            return "colecciones/all";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }


    @GetMapping("/get/{id_coleccion}")
    @PreAuthorize("hasAnyRole('VISUALIZADOR', 'CONTRIBUYENTE', 'ADMINISTRADOR')")
    public String getColeccion(@PathVariable Long id_coleccion, Model model){
        ResponseEntity<?> rta = coleccionService.getColeccion(id_coleccion);
        if (rta.getStatusCode().is2xxSuccessful() && rta.getBody() != null){
            ColeccionOutputDTO coleccion = (ColeccionOutputDTO) rta.getBody();
            model.addAttribute("coleccion", coleccion);
            return "colecciones/"+coleccion.getId();
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String deleteColeccion(@Valid @RequestParam Long id_coleccion, Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = coleccionService.deleteColeccion(id_coleccion);

        if (rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("mensaje", "Se eliminó correctamente la colección");
            ra.addFlashAttribute("tipo", "success");
            return "redirect:/delete";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String updateColeccion(@Valid @ModelAttribute ColeccionInputDTO inputDTO, Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = coleccionService.updateColeccion(inputDTO);

        if (rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("mensaje", "Se actualizó correctamente la colección");
            ra.addFlashAttribute("tipo", "success");
            return "redirect:/update";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PostMapping("/add/fuente")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String agregarFuente(@Valid @RequestParam Long id_coleccion, @Valid @RequestParam String dataSet, Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = coleccionService.agregarFuente(id_coleccion, dataSet);

        if (rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("mensaje", "Se agregó correctamente la fuente");
            ra.addFlashAttribute("tipo", "success");
            return "redirect:/add/fuente";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }
    @PostMapping("/delete/fuente")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminarFuente(@Valid @RequestParam Long id_coleccion, @Valid @RequestParam Long id_dataset, Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = coleccionService.eliminarFuente(id_coleccion, id_dataset);

        if (rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("mensaje", "Se eliminó correctamente la fuente");
            ra.addFlashAttribute("tipo", "success");
            return "redirect:/delete/fuente";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PostMapping("/modificar-consenso")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String modificarAlgoritmoConsenso(@Valid @ModelAttribute ModificarConsensoInputDTO input, Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = coleccionService.modificarAlgoritmoConsenso(input);

        if (rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("mensaje", "Se modificó correctamente el algoritmo de consenso asociado a la colección " + input.getIdColeccion());
            ra.addFlashAttribute("tipo", "success");
            return "redirect:/modificar-consenso";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PostMapping("/refrescar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String refrescarColecciones(@Valid @ModelAttribute RefrescarColeccionesInputDTO input, Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = coleccionService.refrescarColecciones(input);

        if (rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("mensaje", "Se refrescaron las colecciones correctamente ");
            ra.addFlashAttribute("tipo", "success");
            return "redirect:/modificar-consenso";
        }
        return "redirect:/" + rta.getStatusCode().value();
    }



}