package services.impl;

import models.dtos.input.SolicitudHechoEliminarInputDTO;
import models.dtos.input.SolicitudHechoEvaluarInputDTO;
import models.dtos.input.SolicitudHechoInputDTO;
import models.dtos.input.SolicitudHechoModificarInputDTO;
import models.entities.*;
import models.entities.buscadores.BuscadorCategoria;
import models.entities.buscadores.BuscadorPais;
import models.entities.fuentes.FuenteDinamica;
import models.entities.personas.Rol;
import models.entities.personas.Usuario;
import models.repositories.IHechosRepository;
import models.repositories.IPersonaRepository;
import models.repositories.ISolicitudAgregarHechoRepository;
import models.repositories.ISolicitudEliminarHechoRepository;
import models.repositories.ISolicitudModificarHechoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import services.IDetectorDeSpam;
import services.ISolicitudHechoService;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudHechoService implements ISolicitudHechoService {

    private final ISolicitudAgregarHechoRepository solicitudAgregarHechoRepo;
    private final ISolicitudEliminarHechoRepository solicitudEliminarHechoRepo;
    private final ISolicitudModificarHechoRepository solicitudModificarHechoRepo;
    private final IHechosRepository hechosRepository;
    private final IPersonaRepository usuariosRepository;

    @Autowired
    private IDetectorDeSpam detectorDeSpam;

    GestorRoles gestorRoles;

    public SolicitudHechoService(ISolicitudAgregarHechoRepository solicitudAgregarHechoRepo, ISolicitudEliminarHechoRepository solicitudEliminarHechoRepo,
                                 IPersonaRepository personaRepository, IHechosRepository hechosRepository, IPersonaRepository usuariosRepository, ISolicitudModificarHechoRepository solicitudModificarHechoRepo) {
        this.solicitudAgregarHechoRepo = solicitudAgregarHechoRepo;
        this.solicitudEliminarHechoRepo = solicitudEliminarHechoRepo;
        this.hechosRepository = hechosRepository;
        this.usuariosRepository = usuariosRepository;
        this.solicitudModificarHechoRepo = solicitudModificarHechoRepo;
        gestorRoles = new GestorRoles();
    }

    @Override
    public RespuestaHttp<Void> solicitarSubirHecho(SolicitudHechoInputDTO dto) {

        Usuario usuario = usuariosRepository.findById(dto.getId_usuario());

        if (usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return new RespuestaHttp<>(null, HttpStatus.UNAUTHORIZED.value());
        }

        List<Hecho> hechos = hechosRepository.findAll();

        Optional<Hecho> hecho2 = hechos.stream().filter(h->Normalizador.normalizarYComparar(h.getPais().getPais(), dto.getPais())).findFirst();
        Pais pais;

        if (hecho2.isPresent()){
            pais = hecho2.get().getPais();
        } else {
            pais = new Pais();
            pais.setPais(dto.getPais());
        }

        HechosData hechosData = new HechosData(dto.getTitulo(), dto.getDescripcion(), dto.getTipoContenido(),
                pais, dto.getFechaAcontecimiento(), hechosRepository.getProxId());

        FuenteDinamica fuenteDinamica = new FuenteDinamica();
        Hecho hecho = fuenteDinamica.crearHecho(hechosData);
        SolicitudHecho solicitudHecho = new SolicitudHecho(usuario, hecho, solicitudAgregarHechoRepo.getProxId());
        solicitudAgregarHechoRepo.save(solicitudHecho);
        hecho.setId_usuario(usuario.getId());
        hechosRepository.save(hecho);

        return new RespuestaHttp<>(null, HttpStatus.OK.value());
    }

    //El usuario manda una solicitud para eliminar un hecho -> guardar la solicitud en la base de datos
    @Override
    public RespuestaHttp<Void> solicitarEliminacionHecho(SolicitudHechoEliminarInputDTO dto){
        Usuario usuario = usuariosRepository.findById(dto.getId_usuario());
        if (usuario == null || usuario.getRol().equals(Rol.ADMINISTRADOR) || usuario.getRol().equals(Rol.VISUALIZADOR)){
            return new RespuestaHttp<>(null, HttpStatus.UNAUTHORIZED.value());
        }
        Hecho hecho = hechosRepository.findById(dto.getId_hecho());

        SolicitudHecho solicitud = new SolicitudHecho(usuario, hecho, solicitudEliminarHechoRepo.getProxId(), dto.getJustificacion());

        if (detectorDeSpam.esSpam(dto.getJustificacion())) {
            // Marcar como rechazada por spam y guardar
            solicitud.setProcesada(true);
            solicitud.setRechazadaPorSpam(true);
            solicitudEliminarHechoRepo.save(solicitud);
            return new RespuestaHttp<>(null, HttpStatus.BAD_REQUEST.value()); // 400 - solicitud rechazada por spam
        }


        solicitudEliminarHechoRepo.save(solicitud);
        return new RespuestaHttp<>(null, HttpStatus.OK.value()); // Un admin no deberia solicitar eliminar, los elimina directamente
    }

    public RespuestaHttp<Void> solicitarModificacionHecho(SolicitudHechoModificarInputDTO dto){

        Usuario usuario = usuariosRepository.findById(dto.getId_usuario());

        if (usuario == null || usuario.getId().equals(hechosRepository.findById(dto.getId_hecho()).getId_usuario()) || usuario.getRol().equals(Rol.ADMINISTRADOR) || usuario.getRol().equals(Rol.VISUALIZADOR)){
            return new RespuestaHttp<>(null, HttpStatus.UNAUTHORIZED.value());
        }

        Hecho hecho = hechosRepository.findById(dto.getId_hecho());

        hecho.setTitulo(dto.getTitulo());
        hecho.setPais(BuscadorPais.buscar(hechosRepository.findAll(), dto.getPais()));
        hecho.setCategoria(BuscadorCategoria.buscar(hechosRepository.findAll(), dto.getPais()));
        hecho.setTitulo(dto.getTitulo());
        hecho.setFechaAcontecimiento(FechaParser.parsearFecha(dto.getFechaAcontecimiento()));
        hecho.setFechaDeCarga(ZonedDateTime.now()); // Nueva fecha de modificación
        hecho.setContenidoMultimedia(TipoContenido.fromCodigo(dto.getTipoContenido()));

        SolicitudHecho solicitud = new SolicitudHecho(usuario, hecho, solicitudModificarHechoRepo.getProxId());
        solicitudModificarHechoRepo.save(solicitud);
        return new RespuestaHttp<>(null, HttpStatus.OK.value());
    }


    @Override
    public RespuestaHttp<Void> evaluarSolicitudSubirHecho(SolicitudHechoEvaluarInputDTO dtoInput) {

        SolicitudHecho solicitud = solicitudAgregarHechoRepo.findById(dtoInput.getId_solicitud());

        if (solicitud == null) {
            return new RespuestaHttp<>(null, HttpStatus.NOT_FOUND.value());
        }

        // Verificar que no haya sido procesada ya
        if (solicitud.isProcesada()) {
            return new RespuestaHttp<>(null, HttpStatus.CONFLICT.value()); // Ya fue procesada
        }

        if (ChronoUnit.DAYS.between(solicitud.getHecho().getFechaDeCarga(), ZonedDateTime.now()) >= 7) {
            return new RespuestaHttp<>(null, HttpStatus.CONFLICT.value());
        }

        Usuario usuario = usuariosRepository.findById(dtoInput.getId_usuario());//el que ejecuta la acción

        if (!usuario.getRol().equals(Rol.ADMINISTRADOR)) {
            return new RespuestaHttp<>(null, HttpStatus.UNAUTHORIZED.value());
        }

        // Marcar como procesada
        solicitud.setProcesada(true);

        if (dtoInput.getRespuesta()) {
            // Aceptar solicitud - marcar hecho como inactivo
            solicitud.getHecho().setActivo(false);
            solicitud.getUsuario().disminuirHechosSubidos();
            hechosRepository.update(solicitud.getHecho());

            if (solicitud.getUsuario().getCantHechosSubidos() == 0) {
                gestorRoles.ContribuyenteAVisualizador(solicitud.getUsuario());
            }
        }
        // Si es false, simplemente queda rechazada (procesada = true pero sin cambios en el hecho)

        solicitudEliminarHechoRepo.update(solicitud);
        return new RespuestaHttp<>(null, HttpStatus.OK.value());
    }

    @Override
    public RespuestaHttp<Void> evaluarEliminacionHecho(SolicitudHechoEvaluarInputDTO dtoInput) {

        SolicitudHecho solicitud = solicitudEliminarHechoRepo.findById(dtoInput.getId_solicitud());
        if (ChronoUnit.DAYS.between(solicitud.getHecho().getFechaDeCarga(), ZonedDateTime.now()) >= 7){
            return new RespuestaHttp<>(null, HttpStatus.CONFLICT.value()); // Error 409: cuando la solicitud es válida pero no puede procesarse por estado actual del recurso
        }
        Usuario usuario = usuariosRepository.findById(dtoInput.getId_usuario());//el que ejecuta la acción

        if(!usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return new RespuestaHttp<>(null, HttpStatus.UNAUTHORIZED.value());
        }
        else {
            if (dtoInput.getRespuesta()) {


                solicitud.getUsuario().disminuirHechosSubidos();
                hechosRepository.update(solicitud.getHecho());

                if (solicitud.getUsuario().getCantHechosSubidos() == 0){
                    gestorRoles.ContribuyenteAVisualizador(solicitud.getUsuario());
                }
            }

        }
        solicitudEliminarHechoRepo.delete(solicitud);
        return new RespuestaHttp<>(null, HttpStatus.OK.value());
    }

    @Override
    public RespuestaHttp<Void> evaluarModificacionHecho(SolicitudHechoEvaluarInputDTO dtoInput) {

        SolicitudHecho solicitud = solicitudModificarHechoRepo.findById(dtoInput.getId_solicitud());
        Usuario usuario = usuariosRepository.findById(dtoInput.getId_usuario());//el que ejecuta la acción

        if(!usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return new RespuestaHttp<>(null, HttpStatus.UNAUTHORIZED.value());
        }
        else {
            if (dtoInput.getRespuesta()) {
                // El hecho debe modificarse
                hechosRepository.getSnapshotHechos().add(solicitud.getHecho());
                hechosRepository.update(solicitud.getHecho());
            }
        }
        solicitudModificarHechoRepo.delete(solicitud);
        return new RespuestaHttp<>(null, HttpStatus.OK.value());
    }

    @Override
    public List<SolicitudHecho> obtenerSolicitudesPendientes() {
        return solicitudEliminarHechoRepo.findAll().stream()
                .filter(s -> !s.isProcesada() && !s.isRechazadaPorSpam())
                .toList();
    }

    
}
