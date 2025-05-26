package services;

import models.dtos.input.SolicitudHechoEliminarInputDTO;
import models.dtos.input.SolicitudHechoEvaluarInputDTO;
import models.dtos.input.SolicitudHechoInputDTO;
import models.dtos.input.SolicitudHechoModificarInputDTO;
import models.entities.Hecho;
import models.entities.RespuestaHttp;
import models.entities.SolicitudHecho;
import models.entities.personas.Usuario;

import java.util.List;

public interface ISolicitudHechoService {
    public RespuestaHttp<Void> solicitarSubirHecho(SolicitudHechoInputDTO dto);
    public RespuestaHttp<Void> evaluarSolicitudSubirHecho(SolicitudHechoEvaluarInputDTO dto);
    public RespuestaHttp<Void> evaluarEliminacionHecho(SolicitudHechoEvaluarInputDTO dto);
    public RespuestaHttp<Void> solicitarEliminacionHecho(SolicitudHechoEliminarInputDTO dto);
    public RespuestaHttp<Void> solicitarModificacionHecho(SolicitudHechoModificarInputDTO dto);
    public RespuestaHttp<Void> evaluarModificacionHecho(SolicitudHechoEvaluarInputDTO dtoInput);
    public List<SolicitudHecho> obtenerSolicitudesPendientes();
}
