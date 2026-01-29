package com.tpagrupo17.Metamapa.dtos.input;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class SolicitudHechoInputDTO { //datos del hecho y el id del usuario

    private String titulo;

    private String descripcion;
    private String fechaAcontecimiento;

    private Double latitud;
    private Double longitud;

    private Long id_pais;
    private Long id_categoria;
    private Long id_provincia;

    private List<MultipartFile> contenidosMultimedia;
}
