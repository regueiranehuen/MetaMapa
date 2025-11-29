package modulos.agregacion.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import modulos.AtributosHechoMemoria;
import modulos.agregacion.entities.DbEstatica.Dataset;
import modulos.agregacion.entities.atributosHecho.AtributosHecho;
import modulos.agregacion.entities.atributosHecho.AtributosHechoModificar;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class HechoMemoria {
    private Long id;

    private Boolean activo;

    private Long usuario_id;

    private AtributosHechoMemoria atributosHecho;


    private List<Dataset> datasets;

    public HechoMemoria(Long id, Boolean activo, Long usuario_id) {
        this.id = id;
        this.activo = activo;
        this.usuario_id = usuario_id;
        this.atributosHecho = new AtributosHechoMemoria();
    }

    public HechoMemoria(){
        this.atributosHecho = new AtributosHechoMemoria();
        this.datasets = new ArrayList<>();
    }
}
