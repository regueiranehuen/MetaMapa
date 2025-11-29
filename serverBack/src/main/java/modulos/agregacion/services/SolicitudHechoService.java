package modulos.agregacion.services;

import io.jsonwebtoken.Jwt;
import jakarta.transaction.Transactional;
import modulos.JwtClaimExtractor;
import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbDinamica.solicitudes.*;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import modulos.agregacion.entities.atributosHecho.AtributosHechoModificar;
import modulos.agregacion.entities.DbMain.projections.SolicitudHechoProjection;
import modulos.agregacion.entities.atributosHecho.ContenidoMultimedia;
import modulos.agregacion.entities.atributosHecho.TipoContenido;
import modulos.agregacion.repositories.DbDinamica.*;
import modulos.agregacion.repositories.DbEstatica.IHechosEstaticaRepository;
import modulos.agregacion.repositories.DbMain.*;
import modulos.agregacion.repositories.DbProxy.IHechosProxyRepository;
import modulos.buscadores.BuscadorPais;
import modulos.buscadores.BuscadorProvincia;
import modulos.buscadores.BuscadorUbicacion;
import modulos.shared.dtos.input.*;
import modulos.shared.dtos.output.*;
import modulos.shared.utils.DetectorDeSpam;
import modulos.shared.utils.FechaParser;
import modulos.agregacion.entities.fuentes.FuenteDinamica;
import modulos.shared.utils.GestorArchivos;
import modulos.shared.utils.GestorRoles;
import modulos.agregacion.entities.DbMain.usuario.Rol;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import modulos.agregacion.entities.atributosHecho.AtributosHecho;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudHechoService {

    private final ISolicitudAgregarHechoRepository solicitudAgregarHechoRepo;
    private final ISolicitudEliminarHechoRepository solicitudEliminarHechoRepo;
    private final ISolicitudModificarHechoRepository solicitudModificarHechoRepo;
    private final IHechosDinamicaRepository hechosDinamicaRepository;
    private final IUsuarioRepository usuariosRepository;
    private final IMensajeRepository mensajesRepository;
    private final IReporteHechoRepository reportesHechoRepository;
    private final ISolicitudRepository solicitudRepository;
    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final ICategoriaRepository categoriaRepository;
    private final BuscadorUbicacion buscadorUbicacion;
    private final BuscadorPais buscadorPais;
    private final BuscadorProvincia buscadorProvincia;
    private final IHechosEstaticaRepository hechosEstaticaRepository;
    private final IHechosProxyRepository hechosProxyRepository;
    private final IHechoRefRepository hechoRefRepository;

    public SolicitudHechoService(ISolicitudAgregarHechoRepository solicitudAgregarHechoRepo, IHechoRefRepository hechoRefRepository, IHechosProxyRepository hechosProxyRepository, ISolicitudEliminarHechoRepository solicitudEliminarHechoRepo, ISolicitudModificarHechoRepository solicitudModificarHechoRepo, IHechosDinamicaRepository hechosDinamicaRepository, IUsuarioRepository usuariosRepository, IMensajeRepository mensajesRepository, IReporteHechoRepository reportesHechoRepository, ISolicitudRepository solicitudRepository, IPaisRepository paisRepository, IProvinciaRepository provinciaRepository, ICategoriaRepository categoriaRepository, BuscadorUbicacion buscadorUbicacion, BuscadorPais buscadorPais, BuscadorProvincia buscadorProvincia, IHechosEstaticaRepository hechosEstaticaRepository) {
        this.solicitudAgregarHechoRepo = solicitudAgregarHechoRepo;
        this.solicitudEliminarHechoRepo = solicitudEliminarHechoRepo;
        this.solicitudModificarHechoRepo = solicitudModificarHechoRepo;
        this.hechosDinamicaRepository = hechosDinamicaRepository;
        this.usuariosRepository = usuariosRepository;
        this.mensajesRepository = mensajesRepository;
        this.reportesHechoRepository = reportesHechoRepository;
        this.solicitudRepository = solicitudRepository;
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
        this.categoriaRepository = categoriaRepository;
        this.buscadorUbicacion = buscadorUbicacion;
        this.buscadorPais = buscadorPais;
        this.buscadorProvincia = buscadorProvincia;
        this.hechosEstaticaRepository = hechosEstaticaRepository;
        this.hechosProxyRepository = hechosProxyRepository;
        this.hechoRefRepository = hechoRefRepository;
    }

    private ResponseEntity<?> checkeoAdmin(String username){
        Usuario usuario = username != null ? usuariosRepository.findByNombreDeUsuario(username).orElse(null) : null;

        if (usuario == null || !usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tenés permisos para ejecutar esta acción");
        }
        return ResponseEntity.ok(usuario);
    }

    private ResponseEntity<?> checkeoContribuyente(String username){
        Usuario usuario = username != null ? usuariosRepository.findByNombreDeUsuario(username).orElse(null) : null;

        if (usuario == null || !usuario.getRol().equals(Rol.CONTRIBUYENTE)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tenés permisos para ejecutar esta acción");
        }
        return ResponseEntity.ok(usuario);
    }

    private ResponseEntity<?> checkeoNoAdmin(String username){

        Usuario usuario = username != null ? usuariosRepository.findByNombreDeUsuario(username).orElse(null) : null;

        if (usuario == null || !usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tenés permisos para ejecutar esta acción");
    }

    @Transactional
    public ResponseEntity<?> solicitarSubirHecho(SolicitudHechoInputDTO dto, List<MultipartFile> files, String username){
        // Los visualizadores o contribuyentes llaman al metodo, no los admins
        Usuario usuario = null;
        usuario = usuariosRepository.findByNombreDeUsuario(username).orElse(null);
        if (username!=null && !username.isEmpty()){
            ResponseEntity<?> rta = this.checkeoNoAdmin(username);

            if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
                return rta;
            }

            usuario = (Usuario) rta.getBody();
        }


        Pais pais = dto.getId_pais() != null ? paisRepository.findById(dto.getId_pais()).orElse(null) : null;
        Provincia provincia = dto.getId_provincia() != null ? provinciaRepository.findById(dto.getId_provincia()).orElse(null) : null;
        Categoria categoria = dto.getId_categoria() != null ? categoriaRepository.findById(dto.getId_categoria()).orElse(null) : null;
        Long categoria_id = categoria != null ? categoria.getId() : null;
        Ubicacion ubicacion = buscadorUbicacion.buscarOCrear(pais, provincia);
        Long ubicacion_id = ubicacion != null ? ubicacion.getId() : null;

        List<ContenidoMultimedia> contenidosMultimedia = new ArrayList<>();

        System.out.println("VOY A ENTRAR A CONTENIDO MULTIMEDIA");
        if (files != null){
            System.out.println("ENTRE!! QUE EMOCION");
            for(MultipartFile file : files) {
                try {
                    String url = GestorArchivos.guardarArchivo(file);

                    ContenidoMultimedia contenidoMultimedia = new ContenidoMultimedia();

                    contenidoMultimedia.setUrl(url);
                    contenidoMultimedia.almacenarTipoDeArchivo(file.getContentType());
                    contenidosMultimedia.add(contenidoMultimedia);
                } catch (IOException e) {
                    System.err.println("❌ Error al procesar archivos multimedia:");
                }

            }
        }



        FuenteDinamica fuenteDinamica = new FuenteDinamica();
        HechoDinamica hecho = fuenteDinamica.crearHecho(dto, contenidosMultimedia, categoria_id, ubicacion_id);

        SolicitudSubirHecho solicitudHecho = new SolicitudSubirHecho();
        solicitudHecho.setFecha(LocalDateTime.now());
        if(usuario!=null) {
            solicitudHecho.setUsuario_id(usuario.getId());
            hecho.setUsuario_id(usuario.getId());

        } else{
            solicitudHecho.setUsuario_id(null);
            hecho.setUsuario_id(null);
        }

        if (DetectorDeSpam.esSpam(dto.getTitulo()) || DetectorDeSpam.esSpam(dto.getDescripcion())) {
            System.out.println("SOY UNA x AL IGUAL QUE EL DETECTOR DE SPAM");
            solicitudHecho.setProcesada(true);
            solicitudHecho.setRechazadaPorSpam(true);
            hechosDinamicaRepository.saveAndFlush(hecho);
            solicitudHecho.setHecho(hecho);
            solicitudAgregarHechoRepo.save(solicitudHecho);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("La solicitud fue rechazada automáticamente por spam");
        }

        hecho.getAtributosHecho().setFuente(Fuente.DINAMICA);

        hechosDinamicaRepository.save(hecho);
        solicitudHecho.setHecho(hecho);
        solicitudAgregarHechoRepo.save(solicitudHecho);
        hechoRefRepository.save(new HechoRef(hecho.getId(), hecho.getAtributosHecho().getFuente()));

        return ResponseEntity.status(HttpStatus.OK).body("Se envió su solicitud para subir hecho");
    }

    //El usuario manda una solicitud para eliminar un hecho -> guardar la solicitud en la base de datos
    @Transactional
    public ResponseEntity<?> solicitarEliminacionHecho(SolicitudHechoEliminarInputDTO dto, String username){
        // Un admin no debería solicitar eliminar, los elimina directamente

            ResponseEntity<?> rta = this.checkeoNoAdmin(username);
            Usuario usuario = (Usuario) rta.getBody();
            if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
                return rta;
            }

        // El hecho debe estar asociado al usuario
        HechoDinamica hecho = hechosDinamicaRepository.findByIdAndUsuario(dto.getId_hecho(), usuario.getId()).orElse(null);
        if (hecho == null){
            // Puede ser que se haya encontrado el hecho pero que el usuario no esté asociado al hecho
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tenés permisos para ejecutar esta acción");
        }

        SolicitudEliminarHecho solicitud = new SolicitudEliminarHecho(usuario.getId(), hecho, dto.getJustificacion());
        solicitud.setFecha(LocalDateTime.now());
        if (DetectorDeSpam.esSpam(dto.getJustificacion())) {
            // Marcar como rechazada por spam y guardar
            solicitud.setProcesada(true);
            solicitud.setRechazadaPorSpam(true);
            solicitudEliminarHechoRepo.save(solicitud);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Se detectó spam"); // 400 - solicitud rechazada por spam
        }
        System.out.println("LA JUSTIFICACION TIENE LENGTH: " + solicitud.getJustificacion().length());
        solicitudEliminarHechoRepo.save(solicitud);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Transactional
    public ResponseEntity<?> solicitarModificacionHecho(SolicitudHechoModificarInputDTO dto, String username){

        
        ResponseEntity<?> rta = checkeoContribuyente(username);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }

        Usuario usuario = (Usuario)rta.getBody();

        if (usuario == null){
            return rta;
        }

        HechoDinamica hecho = hechosDinamicaRepository.findByIdAndUsuario(dto.getId_hecho(), usuario.getId()).orElse(null);

        if (hecho == null){
            // El hecho no existe o el usuario no tiene permiso
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tenés permisos para ejecutar esta acción");
        }

        SolicitudModificarHecho solicitud = new SolicitudModificarHecho(usuario.getId(), hecho);
        solicitud.setFecha(LocalDateTime.now());

        /*
        if (DetectorDeSpam.esSpam(dto.getTitulo()) || DetectorDeSpam.esSpam(dto.getDescripcion()))
        {
            solicitud.setProcesada(true);
            solicitud.setRechazadaPorSpam(true);
            solicitudModificarHechoRepo.save(solicitud);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Se detectó spam"); // 400 - solicitud rechazada por spam
        }
        */


        if (ChronoUnit.DAYS.between(hecho.getAtributosHecho().getFechaCarga(), LocalDateTime.now()) >= 7){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Terminó la fecha límite para solicitar modificar el hecho"); // Error 409: cuando la solicitud es válida, pero no puede procesarse por estado actual del recurso
        }

        AtributosHechoModificar atributos = new AtributosHechoModificar();
        Optional.ofNullable(dto.getTitulo()).ifPresent(atributos::setTitulo);

        if(dto.getId_pais() !=null || dto.getId_provincia() != null){
            Pais pais = buscadorPais.buscar(dto.getId_pais());
            Provincia provincia = buscadorProvincia.buscar(dto.getId_provincia());
            Ubicacion ubicacion = buscadorUbicacion.buscarOCrear(pais, provincia);
            atributos.setUbicacion_id(ubicacion != null ? ubicacion.getId() : null);
        }

        if (dto.getLongitud() != null && dto.getLatitud()!=null){
            atributos.setLatitud(dto.getLatitud());
            atributos.setLongitud(dto.getLongitud());
        }

        Optional.ofNullable(dto.getId_categoria()).flatMap(categoriaRepository::findById).
                ifPresent(categoria -> atributos.setCategoria_id(categoria.getId()));


        Optional.ofNullable(dto.getFechaAcontecimiento()).ifPresent(fechaStr -> {
            atributos.setFechaAcontecimiento(FechaParser.parsearFecha(fechaStr));
        });

        List<ContenidoMultimedia> contenidosMultimediaParaAgregar = new ArrayList<>();


        if(dto.getNuevasRutasMultimedia() != null) {
            for (ContenidoMultimediaDTO contenidoMultimediaDTO : dto.getNuevasRutasMultimedia()) {
                ContenidoMultimedia contenidoMultimedia = new ContenidoMultimedia();
                contenidoMultimedia.setUrl(contenidoMultimediaDTO.getUrl());
                contenidoMultimedia.almacenarTipoDeArchivo(contenidoMultimediaDTO.getContentType());
                contenidosMultimediaParaAgregar.add(contenidoMultimedia);
            }

            atributos.setContenidoMultimediaAgregar(contenidosMultimediaParaAgregar);

        }


        Optional.ofNullable(dto.getContenidosMultimediaAEliminar()).ifPresent(atributos::setContenidoMultimediaEliminar);
        Optional.ofNullable(dto.getDescripcion()).ifPresent(atributos::setDescripcion);
        // hecho.getAtributosHechoAModificar().add(atributos);

        hechosDinamicaRepository.save(hecho);
        solicitud.setAtributosModificar(atributos);
        solicitudModificarHechoRepo.save(solicitud);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // @Transacional:
    // Así, la SolicitudHecho que traés con findById queda managed durante el method y all lo que le modifiques
    //  (y a sus asociaciones cargadas) se hace UPDATE al hacer commit, sin llamar a save(...).
    @Transactional
    public ResponseEntity<?> evaluarSolicitudSubirHecho(SolicitudHechoEvaluarInputDTO dtoInput, String username) {

        System.out.println("JUSTIFICACION: " + dtoInput.getMensaje());

        RolCambiadoDTO dto = new RolCambiadoDTO();
        dto.setRolModificado(false);

        SolicitudHecho solicitud = solicitudRepository.findByIdAndProcesadaFalse(dtoInput.getId_solicitud()).orElse(null);

        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró la solicitud");
        }

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }
        Usuario usuario = null;
        if(solicitud.getUsuario_id() != null) {
            usuario = usuariosRepository.findById(solicitud.getUsuario_id()).orElse(null);
        }

        if (dtoInput.getRespuesta()) {
            solicitud.getHecho().setActivo(true);
            solicitud.getHecho().getAtributosHecho().setModificado(true);
            solicitud.getHecho().getAtributosHecho().setFechaCarga(LocalDateTime.now());
            solicitud.getHecho().getAtributosHecho().setFechaUltimaActualizacion(solicitud.getHecho().getAtributosHecho().getFechaCarga()); // Nueva fecha de modificación
            hechosDinamicaRepository.saveAndFlush(solicitud.getHecho());
            if (usuario != null){
                usuario.incrementarHechosSubidos();
                Mensaje mensaje = new Mensaje();
                mensaje.setSolicitud_hecho_id(solicitud.getId());
                mensaje.setReceptor(usuario);
                mensaje.setTextoMensaje("Se aceptó su hecho de título " + solicitud.getHecho().getAtributosHecho().getTitulo());
                mensajesRepository.save(mensaje);
                if (usuario.getRol().equals(Rol.VISUALIZADOR)){
                    dto.setRol(Rol.CONTRIBUYENTE);
                    dto.setRolModificado(true);
                    dto.setUsername(usuario.getNombreDeUsuario());
                    GestorRoles.VisualizadorAContribuyente(usuario);
                }
            }
        } else {
            if (usuario != null) {
                Mensaje mensaje = new Mensaje();
                mensaje.setSolicitud_hecho_id(solicitud.getId());
                mensaje.setReceptor(usuario);
                mensaje.setTextoMensaje("Se rechazó su solicitud de subida del hecho de título " + solicitud.getHecho().getAtributosHecho().getTitulo()
                        + ".\nJustificacion: " + dtoInput.getMensaje());
                mensajesRepository.save(mensaje);
            }
        }
            solicitud.setProcesada(true);

            // Por alguna razon sin saveAndFlush no actualiza el bool procesada en la bdd
            solicitudRepository.saveAndFlush(solicitud);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @Transactional
    public ResponseEntity<?> evaluarEliminacionHecho(SolicitudHechoEvaluarInputDTO dtoInput, String username) {

        RolCambiadoDTO dto = new RolCambiadoDTO();
        dto.setRolModificado(false);

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }

        SolicitudHecho solicitud = solicitudRepository.findByIdAndProcesadaFalse(dtoInput.getId_solicitud()).orElse(null);

        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró la solicitud");
        }

        solicitud.setProcesada(true);
        Usuario usuario = usuariosRepository.findById(solicitud.getUsuario_id()).orElse(null);
        if (dtoInput.getRespuesta()) {
            // No va a haber null pointer exception porque sí o sí hay un usuario asociado al hecho que se solicita eliminar
            solicitud.getHecho().getAtributosHecho().setModificado(true);
            solicitud.getHecho().setActivo(false);
            hechosDinamicaRepository.save(solicitud.getHecho());
            // El usuario va a existir si o si porque ya se verificó cuando solicitó eliminar un hecho, pero x si pide borrar la cuenta hago el chequeo antes
            if (usuario != null){
                usuario.disminuirHechosSubidos();
                Mensaje mensaje = new Mensaje();
                mensaje.setSolicitud_hecho_id(solicitud.getId());
                mensaje.setReceptor(usuario);
                mensaje.setTextoMensaje("Se aceptó su solicitud de eliminar el hecho de título " + solicitud.getHecho().getAtributosHecho().getTitulo());
                mensajesRepository.save(mensaje);
                if (usuario.getCantHechosSubidos() == 0){
                    dto.setRolModificado(true);
                    dto.setRol(Rol.VISUALIZADOR);
                    dto.setUsername(usuario.getNombreDeUsuario());
                    GestorRoles.ContribuyenteAVisualizador(usuario);
                }
            }
        }
        else{
            if (usuario!=null){
                Mensaje mensaje = new Mensaje();
                mensaje.setSolicitud_hecho_id(solicitud.getId());
                mensaje.setReceptor(usuario);
                mensaje.setTextoMensaje("Se rechazó su solicitud de eliminar el hecho de título " + solicitud.getHecho().getAtributosHecho().getTitulo()
                        + ".\nJustificacion: " + dtoInput.getMensaje());
                mensajesRepository.save(mensaje);
            }
        }

        solicitudRepository.save(solicitud);

        return ResponseEntity.ok().body(dto);
    }

    @Transactional
    public ResponseEntity<?> evaluarModificacionHecho(SolicitudHechoEvaluarInputDTO dtoInput, String username) {

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }

        SolicitudModificarHecho solicitud = (SolicitudModificarHecho) solicitudRepository.findByIdAndProcesadaFalse(dtoInput.getId_solicitud()).orElse(null);
        

        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró la solicitud");
        }

        solicitud.setProcesada(true);

        Usuario usuario = usuariosRepository.findById(solicitud.getUsuario_id()).orElse(null);

        if (dtoInput.getRespuesta()) {
            // El hecho debe modificarse
            this.setearModificadoAOficial(solicitud.getHecho(), solicitud.getAtributosModificar());
            solicitud.getHecho().getAtributosHecho().setFechaUltimaActualizacion(LocalDateTime.now());
            solicitud.getHecho().getAtributosHecho().setModificado(true);
            if (usuario!=null){
                this.enviarMensaje(usuario, solicitud, "Se aceptó su solicitud de modificar el hecho de título " + solicitud.getHecho().getAtributosHecho().getTitulo());
            }
            hechosDinamicaRepository.save(solicitud.getHecho());
        }
        else{

            // X si se borró la cuenta del usuario chequeo si es null o no
            if (dtoInput.getMensaje() != null && usuario != null){
                this.enviarMensaje(usuario,solicitud, dtoInput.getMensaje());
            }
        }
        solicitudModificarHechoRepo.save(solicitud);
        return ResponseEntity.ok().build();
    }

    private void setearModificadoAOficial(Hecho hecho, AtributosHechoModificar atributos){

        // Campos “simples”: siempre se pisan, aunque vengan en null
        hecho.getAtributosHecho().setCategoria_id(atributos.getCategoria_id());
        hecho.getAtributosHecho().setDescripcion(atributos.getDescripcion());
        hecho.getAtributosHecho().setFechaAcontecimiento(atributos.getFechaAcontecimiento());
        hecho.getAtributosHecho().setTitulo(atributos.getTitulo());
        hecho.getAtributosHecho().setUbicacion_id(atributos.getUbicacion_id());
        hecho.getAtributosHecho().setLatitud(atributos.getLatitud());
        hecho.getAtributosHecho().setLongitud(atributos.getLongitud());

        // Contenido multimedia a agregar: solo si hay lista
        if (atributos.getContenidoMultimediaAgregar() != null){
            hecho.getAtributosHecho()
                    .getContenidosMultimedia()
                    .addAll(atributos.getContenidoMultimediaAgregar());
        }

        // Contenido multimedia a eliminar: solo si hay ids a eliminar
        if (atributos.getContenidoMultimediaEliminar() != null){
            hecho.getAtributosHecho().getContenidosMultimedia()
                    .removeIf(contenidoMultimedia ->
                            atributos.getContenidoMultimediaEliminar()
                                    .contains(contenidoMultimedia.getId())
                    );
        }
    }


    private void enviarMensaje(Usuario usuario, SolicitudHecho solicitudHecho, String texto){

        Mensaje mensaje = new Mensaje();
        mensaje.setSolicitud_hecho_id(solicitudHecho.getId());
        mensaje.setTextoMensaje(texto);
        mensaje.setReceptor(usuario);
        mensajesRepository.save(mensaje);
    }

    public ResponseEntity<?> getAllSolicitudes(String username) {

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }


        List<SolicitudHecho> solicitudesHechos = solicitudRepository.findAllByProcesadaFalse();
        List<SolicitudHechoOutputDTO> solicitudHechoOutputDTOS = new ArrayList<>();
        for (SolicitudHecho solicitud: solicitudesHechos){
            SolicitudHechoOutputDTO dto = SolicitudHechoOutputDTO.builder()
                    .id(solicitud.getId())
                    .usuarioId(solicitud.getUsuario_id())
                    .hechoId(solicitud.getHecho().getId())
                    .justificacion(solicitud.getJustificacion())
                    .procesada(solicitud.isProcesada())
                    .rechazadaPorSpam(solicitud.isRechazadaPorSpam())
                    .build();
            solicitudHechoOutputDTOS.add(dto);
        }

        return ResponseEntity.status(HttpStatus.OK).body(solicitudHechoOutputDTOS);
    }

    public ResponseEntity<?> obtenerSolicitudesPendientes(String username) {
        ResponseEntity<?> rta = checkeoAdmin(username);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }

        List<SolicitudHecho> solicitudHechos = solicitudRepository.obtenerSolicitudesPendientes();

        List<SolicitudHechoOutputDTO> solicitudHechoOutputDTOS = new ArrayList<>();



        for (SolicitudHecho solicitud: solicitudHechos){
            String nombreUsuario = null;
            if (solicitud.getUsuario_id()!=null){
                Usuario usuario = usuariosRepository.findById(solicitud.getUsuario_id()).orElse(null);
                if (usuario!=null){
                    nombreUsuario = usuario.getNombreDeUsuario();
                }
            }

            SolicitudHechoOutputDTO dto = SolicitudHechoOutputDTO.builder()
                    .id(solicitud.getId())
                    .usuarioId(solicitud.getUsuario_id())
                    .username(nombreUsuario)
                    .hechoId(solicitud.getHecho() != null ? solicitud.getHecho().getId() : null)
                    .justificacion(solicitud.getJustificacion())
                    .procesada(solicitud.isProcesada())
                    .rechazadaPorSpam(solicitud.isRechazadaPorSpam())
                    .tipo(this.clasificacionSolicitud(solicitud))
                    .fecha(solicitud.getFecha() != null ? solicitud.getFecha().toString() : null)
                    .build();
            solicitudHechoOutputDTOS.add(dto);
        }

        return ResponseEntity.status(HttpStatus.OK).body(solicitudHechoOutputDTOS);
    }

    @Transactional
    public ResponseEntity<?> reportarHecho(String motivo, Long id_hecho, String fuente) {

        Hecho hecho = null;

        if (fuente.equals(Fuente.DINAMICA.codigoEnString())){
            hecho = hechosDinamicaRepository.findById(id_hecho).orElse(null);
        }
        else if (fuente.equals(Fuente.ESTATICA.codigoEnString())){
            hecho = hechosEstaticaRepository.findById(id_hecho).orElse(null);
        }

        else if (fuente.equals(Fuente.PROXY.codigoEnString())){
            hecho = hechosProxyRepository.findById(id_hecho).orElse(null);
        }

        if(hecho == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró el hecho");
        }
        HechoRef hechoRef = new HechoRef(hecho.getId(), hecho.getAtributosHecho().getFuente());
        Reporte reporte = new Reporte(motivo, hechoRef);
        reportesHechoRepository.save(reporte);
        return ResponseEntity.ok().build();
    }

    // Para buscar bien el hecho despues con un boton rápido agrego un endpoint en HechoController para obtener hecho por id y fuente
    public ResponseEntity<?> getAllReportes(String username) {
        ResponseEntity<?> rta = checkeoAdmin(username);

        if (!rta.getStatusCode().equals(HttpStatus.OK)) {
            return rta;
        }

        List<Reporte> reportes = reportesHechoRepository.findAllByProcesadoFalse();
        List<ReporteHechoOutputDTO> outputDTOS = new ArrayList<>();

        for (Reporte reporte : reportes){
            ReporteHechoOutputDTO outputDTO = ReporteHechoOutputDTO.builder()
                    .id(reporte.getId())
                    .id_hecho(reporte.getHecho_asociado().getKey().getId())
                    .fuente(reporte.getHecho_asociado().getKey().getFuente().codigoEnString())
                    .motivo(reporte.getMotivo())
                    .build();
            outputDTOS.add(outputDTO);
        }

        return ResponseEntity.status(HttpStatus.OK).body(outputDTOS);
    }

    @Transactional
    public ResponseEntity<?> evaluarReporte(EvaluarReporteInputDTO inputDTO, Jwt principal) {

        RolCambiadoDTO dto = new RolCambiadoDTO();
        dto.setRolModificado(false);

        ResponseEntity<?> rta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (!rta.getStatusCode().equals(HttpStatus.OK)) {
            return rta;
        }
        // Al pedo pq ya está con jakarta en el dto pero bue
        if (inputDTO.getReporte_id() == null) {
            return ResponseEntity.badRequest().build();
        }

        Reporte reporte = reportesHechoRepository.findById(inputDTO.getReporte_id()).orElse(null);

        if (reporte == null) {
            return ResponseEntity.notFound().build();
        }

        reporte.setProcesado(true);
        if (inputDTO.getRespuesta()) {
            Fuente fuente = reporte.getHecho_asociado().getKey().getFuente();
            Long id = reporte.getHecho_asociado().getKey().getId();
            Hecho hecho = null;
            if (fuente.equals(Fuente.ESTATICA)) {
                hecho = hechosEstaticaRepository.findById(id).orElse(null);
            }
            else if (fuente.equals(Fuente.DINAMICA)) {
                hecho = hechosDinamicaRepository.findById(id).orElse(null);
            }
            else if (fuente.equals(Fuente.PROXY)) {
                hecho = hechosProxyRepository.findById(id).orElse(null);
            }
            if (hecho == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró el hecho");
            hecho.setActivo(false);
            hecho.getAtributosHecho().setModificado(true);
            Usuario usuario = usuariosRepository.findById(hecho.getUsuario_id()).orElse(null);
            // x si pide borrar la cuenta hago el chequeo antes
            if (usuario != null) {
                usuario.disminuirHechosSubidos();
                if (usuario.getCantHechosSubidos() == 0) {
                    dto.setRolModificado(true);
                    dto.setRol(Rol.VISUALIZADOR);
                    dto.setUsername(usuario.getNombreDeUsuario());
                    GestorRoles.ContribuyenteAVisualizador(usuario);
                }
            }
        }

        return ResponseEntity.ok().body(dto);

    }

    public ResponseEntity<Integer> getPorcentajeSolicitudesProcesadas() {

        System.out.println("Entre a solicitudes");
        Integer porcentaje = solicitudRepository.porcentajeProcesadas().intValue();
        System.out.println("PORCENTAJE: " + porcentaje);
        return ResponseEntity.ok().body(porcentaje);
    }

    public String clasificacionSolicitud(SolicitudHecho solicitud){
        String tipo = null;

        if (solicitud instanceof SolicitudSubirHecho){
            tipo = "SUBIR";
        }
        else if (solicitud instanceof SolicitudModificarHecho){
            tipo = "MODIFICAR";
        }
        else if (solicitud instanceof SolicitudEliminarHecho){
            tipo = "ELIMINAR";
        }
        return tipo;
    }


    public ResponseEntity<?> getAtributosSolicitudHecho(Long id_solicitud, String username) {

        // --- 1. Solo admin ---
        ResponseEntity<?> rta = checkeoAdmin(username);
        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)) return rta;

        if (id_solicitud == null) return ResponseEntity.notFound().build();

        // --- 2. Buscar la solicitud ---
        SolicitudModificarHecho solicitud = (SolicitudModificarHecho) solicitudModificarHechoRepo.findById(id_solicitud).orElse(null);
        if (solicitud == null) return ResponseEntity.notFound().build();

        AtributosHechoModificar attrs = solicitud.getAtributosModificar();
        Hecho hecho = solicitud.getHecho();                 // hecho original
        AtributosHecho attrOriginal = hecho.getAtributosHecho(); // atributos originales del hecho

        // --- 3. Resolver ubicación (pais + provincia) ---
        String paisNombre = null;
        String provinciaNombre = null;

        Long ubicacionId = attrs.getUbicacion_id();

        if (ubicacionId != null) {
            Ubicacion u = buscadorUbicacion.buscarUbicacion(ubicacionId);

            if (u != null) {
                if (u.getPais() != null) {
                    paisNombre = u.getPais().getPais();
                }
                if (u.getProvincia() != null) {
                    provinciaNombre = u.getProvincia().getProvincia();
                }
            }
        }

        // --- 4. Resolver categoría ---
        Long categoriaId = attrs.getCategoria_id() != null
                ? attrs.getCategoria_id()
                : attrOriginal.getCategoria_id();

        String categoriaNombre = null;

        if (categoriaId != null) {
            categoriaNombre = categoriaRepository.findById(categoriaId)
                    .map(Categoria::getTitulo)
                    .orElse(null);
        }

        List<ContenidoMultimedia> contenidoMultimediaFinal = new ArrayList<>();

        contenidoMultimediaFinal.addAll(attrs.getContenidoMultimediaAgregar());

        List<ContenidoMultimedia> contenidoMultimediaAMantener = new ArrayList<>();
        System.out.println("CONTENIDO A ELIMINAR: " + attrs.getContenidoMultimediaEliminar());

        if (attrs.getContenidoMultimediaEliminar() == null || attrs.getContenidoMultimediaEliminar().isEmpty()){
            contenidoMultimediaAMantener.addAll(attrOriginal.getContenidosMultimedia());
        }
        else{
            List<Long> idsContenidosAEliminar = attrs.getContenidoMultimediaEliminar();
            contenidoMultimediaAMantener.addAll(attrOriginal.getContenidosMultimedia().stream().filter(
                    contenidoMultimediaOriginal -> !idsContenidosAEliminar.contains(contenidoMultimediaOriginal.getId())
            ).toList());
        }

        contenidoMultimediaFinal.addAll(contenidoMultimediaAMantener);

        // --- 5. Armar DTO final ---
        AtributosModificarDTO dto = AtributosModificarDTO.builder()
                .titulo(attrs.getTitulo())
                .descripcion(attrs.getDescripcion())
                .categoria(categoriaNombre)
                .pais(paisNombre)
                .provincia(provinciaNombre)

                // fecha acontecimiento
                .fechaAcontecimiento(
                        attrs.getFechaAcontecimiento() != null
                                ? attrOriginal.getFechaAcontecimiento().toString()
                                : null
                )

                // fecha carga original
                .fechaCarga(
                        attrOriginal.getFechaCarga() != null
                                ? attrOriginal.getFechaCarga().toString()
                                : null
                )

                // coordenadas
                .latitud(attrs.getLatitud())

                .longitud(attrs.getLongitud())

                .contenido(contenidoMultimediaFinal)

                .build();

        return ResponseEntity.ok(dto);
    }


}


