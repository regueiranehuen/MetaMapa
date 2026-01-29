package com.tpagrupo17.Metamapa.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import com.tpagrupo17.Metamapa.utils.BodyToListConverter;
import com.tpagrupo17.Metamapa.dtos.input.SinonimoInputDTO;
import com.tpagrupo17.Metamapa.dtos.output.CategoriaDto;
import com.tpagrupo17.Metamapa.dtos.output.PaisDto;
import com.tpagrupo17.Metamapa.dtos.output.ProvinciaDto;
import com.tpagrupo17.Metamapa.services.CategoriaService;
import com.tpagrupo17.Metamapa.services.HechosService;
import com.tpagrupo17.Metamapa.services.SinonimoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/gestion")
public class GestionController {

    private final CategoriaService categoriaService;
    private final SinonimoService sinonimoService;
    private final HechosService hechosService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONTRIBUYENTE')")
    @GetMapping("/categorias/crear")
    public String cargarFormCrearCategoria(){
        return "crearCategoria";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/categorias/crear")
    public String crearCategoria(@RequestParam String categoria, RedirectAttributes ra) {

        

        ResponseEntity<?> rta = this.categoriaService.crearCategoria(categoria);

        if(rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("msgExito", "Categoría creada correctamente");
            return "redirect:/usuarios/perfil";
        } else if(rta.getBody() != null){
            ra.addFlashAttribute("msgError", rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }



    @PreAuthorize("hasAnyRole('ADMINISTRADOR')")
    @GetMapping("/sinonimos/crear")
    public String cargarFormCrearSinonimo(
            Model model,
            @ModelAttribute("sinonimoForm") SinonimoInputDTO sinonimoInputDTO
    ) {
        // Si entro por primera vez, pongo form vacío
        if (sinonimoInputDTO == null) {
            sinonimoInputDTO = new SinonimoInputDTO();
        }

        if (!model.containsAttribute("sinonimoForm")) {
            model.addAttribute("sinonimoForm", sinonimoInputDTO);
        }

        // Traigo países y categorías
        ResponseEntity<?> rtaPaises = hechosService.getPaises();
        ResponseEntity<?> rtaCategorias = hechosService.getCategorias();

        if (!rtaPaises.getStatusCode().is2xxSuccessful()
                || !rtaCategorias.getStatusCode().is2xxSuccessful()) {
            return "redirect:/404";
        }

        List<PaisDto> paises = BodyToListConverter.bodyToList(rtaPaises, PaisDto.class);
        List<CategoriaDto> categorias = BodyToListConverter.bodyToList(rtaCategorias, CategoriaDto.class);

        model.addAttribute("paises", paises);
        model.addAttribute("categorias", categorias);

        // ------------------------------------------
        // LÓGICA DE RECARGA DE PROVINCIAS (un solo país)
        // ------------------------------------------

        if (sinonimoInputDTO.getId_pais() != null) {
            ResponseEntity<?> rtaProvincia = hechosService.getProvinciasByIdPais(sinonimoInputDTO.getId_pais());

            if (rtaProvincia.getBody() != null) {
                List<ProvinciaDto> provincias = BodyToListConverter.bodyToList(rtaProvincia, ProvinciaDto.class);
                model.addAttribute("provincias", provincias);
            }
        }

        // Setear nuevamente el form en el modelo
        model.addAttribute("sinonimoForm", sinonimoInputDTO);

        return "crearSinonimo";
    }



    @PreAuthorize("hasAnyRole('ADMINISTRADOR')")
    @PostMapping("/sinonimos/crear")
    public String crearSinonimo(@Valid @ModelAttribute("sinonimoForm") SinonimoInputDTO dtoInput, RedirectAttributes ra) {
        ResponseEntity<?> rta = null;

        
        
        

        if (dtoInput.getTipo().equals("categoria")){
            rta = this.sinonimoService.crearSinonimoCategoria(dtoInput);
        } else if (dtoInput.getTipo().equals("pais")){
            rta = this.sinonimoService.crearSinonimoPais(dtoInput);
        } else if (dtoInput.getTipo().equals("provincia")){
            rta = this.sinonimoService.crearSinonimoProvincia(dtoInput);
        }

        if (rta!=null){
            if(rta.getStatusCode().is2xxSuccessful()){
                ra.addFlashAttribute("msgExito", "Sinónimo creado correctamente");
                return "redirect:/usuarios/perfil";
            } else if(rta.getBody() != null){
                ra.addFlashAttribute("msgError", rta.getBody().toString());
            }
            return "redirect:/" + rta.getStatusCode().value();
        }

        return "redirect:/404";

    }
}