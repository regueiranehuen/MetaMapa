package modulos.agregacion.services;

import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import modulos.JwtClaimExtractor;
import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbDinamica.solicitudes.SolicitudHecho;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.agregacion.entities.atributosHecho.*;
import modulos.agregacion.entities.DbMain.filtros.*;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import modulos.agregacion.entities.HechoMemoria;
import modulos.agregacion.entities.fuentes.Responses.HechoMetamapaResponse;
import modulos.agregacion.repositories.DbDinamica.IHechosDinamicaRepository;
import modulos.agregacion.repositories.DbEstatica.IDatasetsRepository;
import modulos.agregacion.repositories.DbEstatica.IHechosEstaticaRepository;
import modulos.agregacion.repositories.DbMain.*;
import modulos.agregacion.repositories.DbProxy.IHechosProxyRepository;
import modulos.shared.dtos.output.*;
import modulos.shared.utils.*;
import modulos.agregacion.entities.DbEstatica.Dataset;
import modulos.buscadores.*;
import modulos.shared.dtos.input.*;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForOffsetDateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import modulos.agregacion.entities.fuentes.FuenteEstatica;
import modulos.agregacion.entities.DbMain.usuario.Rol;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
import modulos.shared.dtos.input.ImportacionHechosInputDTO;
import modulos.shared.dtos.input.SolicitudHechoInputDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class HechosService {


    private final IHechosEstaticaRepository hechosEstaticaRepo;
    private final IHechosDinamicaRepository hechosDinamicaRepo;
    private final IHechosProxyRepository hechosProxyRepo;
    private final IPaisRepository repoPais;
    private final IProvinciaRepository repoProvincia;
    private final IUsuarioRepository usuariosRepo;
    private final IColeccionRepository coleccionRepo;
    private final IDatasetsRepository datasetsRepo;
    private final BuscadoresRegistry buscadores;
    private final ICategoriaRepository categoriaRepository;
    private final ISinonimoRepository repoSinonimo;
    private final IHechoRefRepository hechoRefRepository;
    private final IDatasetsRepository repoFuentes;
    private FormateadorHechoMemoria formateadorHechoMemoria;
    private final IMensajeRepository mensajeRepository;
    private final BuscadorUbicacion buscadorUbicacion;
    private final BuscadorPais buscadorPais;
    private final BuscadorProvincia buscadorProvincia;



    /*

    Resumen lógico

Si solo cambian hechos → reviso solo esos hechos contra todas las colecciones.

Si solo cambian colecciones → reviso todos los hechos contra esas colecciones.

Si cambian ambos →

Para colecciones modificadas → reviso todos los hechos.

Para colecciones no modificadas → reviso solo los hechos cambiados

    */

    // MODIFICADO -> TRUE SI HAY QUE EVALUARLO
    // MODIFICADO -> FALSE SI NO!!!!

    private ResponseEntity<?> checkeoAdmin(String username){

        if (username == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Usuario usuario = usuariosRepo.findByNombreDeUsuario(username).orElse(null);

        if (usuario == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el usuario");
        }
        else if (!usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuario);
    }


    //lo sube un administrador (lo considero carga dinamica)
    @Transactional
    public ResponseEntity<?> subirHecho(SolicitudHechoInputDTO dtoInput, List<MultipartFile> files, String username){

        if(dtoInput.getTitulo() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (!rta.getStatusCode().equals(HttpStatus.OK)){
            return rta;
        }

        Usuario usuario = (Usuario)rta.getBody();
        assert usuario != null;
        usuario.incrementarHechosSubidos();

        HechoDinamica hecho = new HechoDinamica();

        System.out.println("SOY UN ID PAIS CONTENTO: " + dtoInput.getId_pais());
        AtributosHecho atributos = FormateadorHecho.formatearAtributosHecho(buscadores, dtoInput);

        hecho.setUsuario_id(usuario.getId());
        hecho.setAtributosHecho(atributos);
        hecho.setActivo(true);
        hecho.getAtributosHecho().setModificado(true);
        hecho.getAtributosHecho().setFuente(Fuente.DINAMICA);
        LocalDateTime fecha = LocalDateTime.now();
        System.out.printf("FECHA:" + fecha);
        hecho.getAtributosHecho().setFechaCarga(fecha);
        System.out.println("FECHA:" + fecha);
        hecho.getAtributosHecho().setFechaUltimaActualizacion(hecho.getAtributosHecho().getFechaCarga());

        if (files != null){
            for(MultipartFile contenidoMultimedia : files){
                System.out.println("VOY A GUARDAR UN CONTENIDO MULTIMEDIA");
                this.guardarContenidoMultimedia(contenidoMultimedia, hecho);
            }
        }


        hechosDinamicaRepo.save(hecho);
        hechoRefRepository.save(new HechoRef(hecho.getId(), hecho.getAtributosHecho().getFuente()));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private void guardarContenidoMultimedia(MultipartFile file, Hecho hecho) {
        try {
            String url = GestorArchivos.guardarArchivo(file);

            ContenidoMultimedia contenidoMultimedia = new ContenidoMultimedia();

            contenidoMultimedia.setUrl(url);
            contenidoMultimedia.almacenarTipoDeArchivo(file.getContentType());
            hecho.getAtributosHecho().getContenidosMultimedia().add(contenidoMultimedia);
        } catch (IOException e) {
            System.err.println("❌ Error al procesar archivos multimedia:");
            e.printStackTrace();
        }
    }

    @Transactional
    public ResponseEntity<?> importarHechos(ImportacionHechosInputDTO dtoInput, MultipartFile file, String username) {
        try {

            System.out.println("ENTRE AL BACK JIJI: " + file.getContentType() + " " + dtoInput.getFuenteString());

            ResponseEntity<?> rta = checkeoAdmin(username);

            if (!rta.getStatusCode().equals(HttpStatus.OK)) {
                return rta;
            }

            Usuario usuario = (Usuario) rta.getBody();

            // 1) Validaciones básicas
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Debés adjuntar un archivo CSV");
            }
            if (!Objects.equals(file.getContentType(), "text/csv") && !Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("El archivo debe ser CSV");
            }

            // === Opción A: guardar en disco ===
            Path base = Paths.get("uploads/datasets").toAbsolutePath().normalize();
            System.out.println("PATH BASE: " + base);
            Files.createDirectories(base);
            String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destino = base.resolve(storedName);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            FuenteEstatica fuente = new FuenteEstatica();


            System.out.println("FUENTE: " + dtoInput.getFuenteString());

            Dataset dataset = datasetsRepo.findByFuente(dtoInput.getFuenteString()).orElse(null);
            if (dataset == null) {
                dataset = new Dataset(dtoInput.getFuenteString());
                dataset.setStoragePath(destino.toString());
                datasetsRepo.save(dataset);
            } else {
                dataset.setStoragePath(destino.toString());
            }

            System.out.println("ARCHIVO A LEER: " + dataset.getStoragePath());

            fuente.setDataSet(dataset);

            List<HechoEstatica> hechos = fuente.leerFuente((Usuario) rta.getBody(), buscadores);

            for (HechoEstatica hecho : hechos) {
                System.out.println("VOY A SUBIR ESTE HECHO: " + hecho.getAtributosHecho().getTitulo());
                hecho.setActivo(true);
                LocalDateTime fechaActual = LocalDateTime.now();
                hecho.getAtributosHecho().setFechaCarga(fechaActual);
                hecho.getAtributosHecho().setFechaUltimaActualizacion(fechaActual);



                hechosEstaticaRepo.saveAndFlush(hecho);
                hechoRefRepository.saveAndFlush(new HechoRef(hecho.getId(), hecho.getAtributosHecho().getFuente()));
            }


            this.enviarMensaje(usuario, "Se importaron los hechos del dataset " + dtoInput.getFuenteString() + " correctamente");
            return ResponseEntity.status(HttpStatus.CREATED).body("Se importaron los hechos correctamente");
        } catch (IOException io) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo debe ser CSV");
        }
    }

    private void enviarMensaje(Usuario usuario, String texto){
        Mensaje mensaje = new Mensaje();
        mensaje.setSolicitud_hecho_id(null);
        mensaje.setTextoMensaje(texto);
        mensaje.setReceptor(usuario);
        mensajeRepository.save(mensaje);
    }


    public ResponseEntity<?> getHechosColeccion(GetHechosColeccionInputDTO inputDTO){
        // TODO: Criterio de fuente

        CriteriosColeccionDTO criterios;

        if(inputDTO.getProvinciaId() != null)
            inputDTO.getProvinciaId().forEach(a -> System.out.println("ID DE LA PROVINCIA: " + a));

        if(OrigenConexion.fromCodigo(inputDTO.getOrigenConexion()).equals(OrigenConexion.FRONT)) {
            criterios = CriteriosColeccionDTO.builder()
                    .categoriaId(inputDTO.getCategoriaId())
                    .contenidoMultimedia(inputDTO.getContenidoMultimedia())
                    .descripcion(inputDTO.getDescripcion())
                    .fechaAcontecimientoInicial(inputDTO.getFechaAcontecimientoInicial())
                    .fechaAcontecimientoFinal(inputDTO.getFechaAcontecimientoFinal())
                    .fechaCargaInicial(inputDTO.getFechaCargaInicial())
                    .fechaCargaFinal(inputDTO.getFechaCargaFinal())
                    .fuentes(inputDTO.getFuentes())
                    .paisId(inputDTO.getPaisId())
                    .titulo(inputDTO.getTitulo())
                    .provinciaId(inputDTO.getProvinciaId())
                    .build();

        } else if (OrigenConexion.fromCodigo(inputDTO.getOrigenConexion()).equals(OrigenConexion.PROXY)){

            List<Long> categoriasId = new ArrayList<>();
            List<Long> paisesId = new ArrayList<>();
            List<Long> provinciasId = new ArrayList<>();

            inputDTO.getCategoria().stream()
                    .map(buscadores.getBuscadorCategoria()::buscar)
                    .filter(Objects::nonNull)
                    .map(Categoria::getId)
                    .forEach(categoriasId::add);

            inputDTO.getPais().stream()
                    .map(buscadores.getBuscadorPais()::buscar)
                    .filter(Objects::nonNull)
                    .map(Pais::getId)
                    .forEach(paisesId::add);

            inputDTO.getProvincia().stream()
                    .map(buscadores.getBuscadorProvincia()::buscar)
                    .filter(Objects::nonNull)
                    .map(Provincia::getId)
                    .forEach(provinciasId::add);

            criterios = CriteriosColeccionDTO.builder()
                    .contenidoMultimedia(inputDTO.getContenidoMultimedia())
                    .descripcion(inputDTO.getDescripcion())
                    .fechaAcontecimientoInicial(inputDTO.getFechaAcontecimientoInicial())
                    .fechaAcontecimientoFinal(inputDTO.getFechaAcontecimientoFinal())
                    .fechaCargaInicial(inputDTO.getFechaCargaInicial())
                    .fechaCargaFinal(inputDTO.getFechaCargaFinal())
                    .fuentes(inputDTO.getFuentes())
                    .titulo(inputDTO.getTitulo())
                    .categoriaId(categoriasId)
                    .paisId(paisesId)
                    .provinciaId(provinciasId)
                    .build();

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<List<IFiltro>> filtros = FormateadorHecho.obtenerListaDeFiltros(FormateadorHecho.formatearFiltrosColeccionDinamica(buscadores, criterios));

        for (int i = 0; i < filtros.size(); i++) {
            List<IFiltro> grupo = filtros.get(i);
            System.out.println("🧩 Grupo #" + i + " (" + grupo.size() + " filtro/s):");

            for (IFiltro filtro : grupo) {
                if (filtro instanceof FiltroCategoria fc) {
                    System.out.println("  [FiltroCategoria] id=" + fc.getCategoria().getId() +
                            ", nombre=" + fc.getCategoria().getTitulo());
                } else if (filtro instanceof FiltroContenidoMultimedia fcm) {
                    System.out.println("  [FiltroContenidoMultimedia] tipo=" + fcm.getTipoContenido());
                } else if (filtro instanceof FiltroDescripcion fd) {
                    System.out.println("  [FiltroDescripcion] texto=" + fd.getDescripcion());
                } else if (filtro instanceof FiltroFechaAcontecimiento ffa) {
                    System.out.println("  [FiltroFechaAcontecimiento] desde=" + ffa.getFechaInicial() +
                            ", hasta=" + ffa.getFechaFinal());
                } else if (filtro instanceof FiltroFechaCarga ffc) {
                    System.out.println("  [FiltroFechaCarga] desde=" + ffc.getFechaInicial() +
                            ", hasta=" + ffc.getFechaFinal());
                } else if (filtro instanceof FiltroFuente ff) {
                    System.out.println("  [FiltroFuente] fuente=" + ff.getFuenteDeseada().codigoEnString());
                } else if (filtro instanceof FiltroPais fp) {
                    System.out.println("  [FiltroPais] id=" + fp.getPais().getId() +
                            ", nombre=" + fp.getPais().getPais());
                } else if (filtro instanceof FiltroProvincia fprov) {
                    System.out.println("  [FiltroProvincia] id=" + fprov.getProvincia().getId() +
                            ", nombre=" + fprov.getProvincia().getProvincia());
                } else if (filtro instanceof FiltroTitulo ft) {
                    System.out.println("  [FiltroTitulo] titulo=" + ft.getTitulo());
                } else {
                    System.out.println("  [Otro tipo de filtro] " + filtro.getClass().getSimpleName());
                }
            }
        }

        Coleccion coleccion = coleccionRepo.findByIdAndActivoTrue(inputDTO.getId_coleccion()).orElse(null);

        if (coleccion == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró la colección");
        }

        Specification<HechoEstatica> specsEstatica = this.crearSpecs(filtros, HechoEstatica.class);
        Specification<HechoDinamica> specsDinamica = this.crearSpecs(filtros, HechoDinamica.class);
        Specification<HechoProxy> specsProxy = this.crearSpecs(filtros, HechoProxy.class);
        List<Hecho> hechosFiltrados = new ArrayList<>();
        if(inputDTO.getNavegacionCurada() && coleccion.getAlgoritmoConsenso() != null) {

            List<Long> hechosIds = new ArrayList<>(coleccion.getHechosConsensuados().stream().map(h -> h.getKey().getId()).toList());

            Specification<HechoEstatica> specColeccionEstatica =
                    this.perteneceAColeccionYesConsensuadoSiAplica(hechosIds, HechoEstatica.class);

            Specification<HechoEstatica> specFinalEstatica = Specification
                    .where(this.distinct(HechoEstatica.class))
                    .and(specColeccionEstatica)
                    .and(specsEstatica);

            List<HechoEstatica> hechosFiltradosEstatica = hechosEstaticaRepo.findAll(specFinalEstatica);

            hechosFiltrados.addAll(hechosFiltradosEstatica);

        }else{
            List<Long> hechosIdsEstatica = new ArrayList<>(coleccion.getHechos().stream().
                    filter(h -> h.getKey().getFuente().equals(Fuente.ESTATICA)).map(h -> h.getKey().getId()).toList());
            List<Long> hechosIdsDinamica = new ArrayList<>(coleccion.getHechos().stream().
                    filter(h -> h.getKey().getFuente().equals(Fuente.DINAMICA)).map(h -> h.getKey().getId()).toList());
            List<Long> hechosIdsProxy = new ArrayList<>(coleccion.getHechos().stream().
                    filter(h -> h.getKey().getFuente().equals(Fuente.PROXY)).map(h -> h.getKey().getId()).toList());

            Specification<HechoEstatica> specColeccionEstatica =
                    this.perteneceAColeccionYesConsensuadoSiAplica(hechosIdsEstatica, HechoEstatica.class);
            Specification<HechoDinamica> specColeccionDinamica =
                    this.perteneceAColeccionYesConsensuadoSiAplica(hechosIdsDinamica, HechoDinamica.class);
            Specification<HechoProxy> specColeccionProxy =
                    this.perteneceAColeccionYesConsensuadoSiAplica(hechosIdsProxy, HechoProxy.class);

            Specification<HechoEstatica> specFinalEstatica = Specification
                    .where(this.distinct(HechoEstatica.class))
                    .and(specColeccionEstatica)
                    .and(specsEstatica);

            Specification<HechoDinamica> specFinalDinamica = Specification
                    .where(this.distinct(HechoDinamica.class))
                    .and(specColeccionDinamica)
                    .and(specsDinamica);

            Specification<HechoProxy> specFinalProxy = Specification
                    .where(this.distinct(HechoProxy.class))
                    .and(specColeccionProxy)
                    .and(specsProxy);

            List<HechoEstatica> hechosFiltradosEstatica = hechosEstaticaRepo.findAll(specFinalEstatica);
            List<HechoDinamica> hechosFiltradosDinamica = hechosDinamicaRepo.findAll(specFinalDinamica);
            List<HechoProxy> hechosFiltradosProxy = hechosProxyRepo.findAll(specFinalProxy);


            hechosFiltrados.addAll(hechosFiltradosEstatica);

            for (HechoDinamica hd: hechosFiltradosDinamica){
                System.out.println("HECHO CON TITULO: " + hd.getAtributosHecho().getTitulo());
            }

            hechosFiltrados.addAll(hechosFiltradosDinamica);
            hechosFiltrados.addAll(hechosFiltradosProxy);

        }

        if (OrigenConexion.fromCodigo(inputDTO.getOrigenConexion()).equals(OrigenConexion.FRONT)) {
            System.out.println("VOY A MAPEAR HECHOS A VISUALIZARHECHOSOUTPUTDTO");
            List<VisualizarHechosOutputDTO> outputDTO = hechosFiltrados.stream()
                    .map(hecho -> crearHechoDto(hecho, VisualizarHechosOutputDTO.class))
                    .toList();
            for (VisualizarHechosOutputDTO hecho: outputDTO){
                System.out.println("HECHO FILTRADO DE LA COLECCION: " + hecho.getTitulo());
            }
            return ResponseEntity.status(HttpStatus.OK).body(outputDTO);
        } else if (OrigenConexion.fromCodigo(inputDTO.getOrigenConexion()).equals(OrigenConexion.PROXY)) {
            List<HechoMetamapaResponse> outputDTO = hechosFiltrados.stream()
                    .map(hecho -> crearHechoDto(hecho, HechoMetamapaResponse.class))
                    .toList();
            return ResponseEntity.status(HttpStatus.OK).body(outputDTO);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private <T> T crearHechoDto(Hecho hecho, Class<T> tipo) {

        System.out.println("ID DEL HECHO: " + hecho.getId());


        HechoMemoria hechoMemoria = formateadorHechoMemoria.formatearHechoMemoria(hecho);

        if (tipo.equals(VisualizarHechosOutputDTO.class)) {

            Usuario usuario = null;
            if(hechoMemoria.getUsuario_id() != null) {
                usuario = usuariosRepo.findById(hechoMemoria.getUsuario_id()).orElse(null);
            }
            VisualizarHechosOutputDTO dto = new VisualizarHechosOutputDTO();
            dto.setId(hecho.getId());
            if(usuario != null) {
                dto.setUsername(usuario.getNombreDeUsuario());
            }
            Optional.ofNullable(hecho.getAtributosHecho().getFechaCarga())
                    .map(Object::toString)
                    .ifPresent(dto::setFechaCarga);

            System.out.println("TITULO HECHO: " + hecho.getAtributosHecho().getTitulo());
            if (hecho.getAtributosHecho().getFuente() != null)
                dto.setFuente(hecho.getAtributosHecho().getFuente().codigoEnString());

            Optional.ofNullable(hecho.getAtributosHecho().getLatitud())
                    .ifPresent(dto::setLatitud);
            Optional.ofNullable(hecho.getAtributosHecho().getLongitud())
                    .ifPresent(dto::setLongitud);

            Optional.ofNullable(hechoMemoria.getAtributosHecho().getUbicacion())
                    .ifPresent(ubicacion -> {
                        Optional.ofNullable(ubicacion.getPais())
                                .ifPresent(pais -> {
                                    dto.setId_pais(pais.getId());
                                    dto.setPais(pais.getPais());
                                });
                        Optional.ofNullable(ubicacion.getProvincia())
                                .ifPresent(provincia -> {
                                    dto.setId_provincia(provincia.getId());
                                    dto.setProvincia(provincia.getProvincia());
                                });
                    });

            dto.setTitulo(hecho.getAtributosHecho().getTitulo());
            dto.setDescripcion(hecho.getAtributosHecho().getDescripcion());
            Optional.ofNullable(hecho.getAtributosHecho().getFechaAcontecimiento())
                    .map(Object::toString)
                    .ifPresent(dto::setFechaAcontecimiento);

            Optional.ofNullable(hechoMemoria.getAtributosHecho().getCategoria())
                    .ifPresent(categoria -> {
                        dto.setId_categoria(categoria.getId());
                        dto.setCategoria(categoria.getTitulo());
                    });
            dto.setContenido(hecho.getAtributosHecho().getContenidosMultimedia());
            return (T) dto;

        } else if (tipo.equals(HechoMetamapaResponse.class)) {
            HechoMetamapaResponse dto = new HechoMetamapaResponse();
            dto.setId(hecho.getId());
            dto.setFuente(hecho.getAtributosHecho().getFuente().codigoEnString());

            Optional.ofNullable(hechoMemoria.getAtributosHecho().getUbicacion())
                    .ifPresent(ubicacion -> {
                        Optional.ofNullable(ubicacion.getPais())
                                .ifPresent(pais -> dto.setPais(pais.getPais()));
                        Optional.ofNullable(ubicacion.getProvincia())
                                .ifPresent(provincia -> dto.setProvincia(provincia.getProvincia()));
                        Optional.ofNullable(hecho.getAtributosHecho().getLatitud())
                                .ifPresent(dto::setLatitud);
                        Optional.ofNullable(hecho.getAtributosHecho().getLongitud())
                                .ifPresent(dto::setLongitud);
                    });

            dto.setTitulo(hecho.getAtributosHecho().getTitulo());
            dto.setDescripcion(hecho.getAtributosHecho().getDescripcion());
            Optional.ofNullable(hecho.getAtributosHecho().getFechaAcontecimiento())
                    .map(Object::toString)
                    .ifPresent(dto::setFechaAcontecimiento);

            Optional.ofNullable(hechoMemoria.getAtributosHecho().getCategoria())
                    .ifPresent(categoria -> dto.setCategoria(categoria.getTitulo()));

            dto.setContenido(hecho.getAtributosHecho().getContenidosMultimedia());

            return (T) dto;
        }

        throw new IllegalArgumentException("Tipo DTO no soportado: " + tipo);
    }

    public ResponseEntity<?> getAllHechos(Integer origen) {

        System.out.println("ORIGEN: " +  origen);

        List<Hecho> hechosTotales = new ArrayList<>();
        hechosTotales.addAll(hechosEstaticaRepo.findAllByActivoTrue());
        hechosTotales.addAll(hechosDinamicaRepo.findAllByActivoTrue());
        hechosTotales.addAll(hechosProxyRepo.findAllByActivoTrue());

        if (OrigenConexion.fromCodigo(origen).equals(OrigenConexion.FRONT)) {
            List<VisualizarHechosOutputDTO> outputDTO = hechosTotales.stream()
                    .map(hecho -> crearHechoDto(hecho, VisualizarHechosOutputDTO.class))
                    .toList();
            return ResponseEntity.status(HttpStatus.OK).body(outputDTO);
        } else if (OrigenConexion.fromCodigo(origen).equals(OrigenConexion.PROXY)) {
            List<HechoMetamapaResponse> outputDTO = hechosTotales.stream()
                    .map(hecho -> crearHechoDto(hecho, HechoMetamapaResponse.class))
                    .toList();
            return ResponseEntity.status(HttpStatus.OK).body(outputDTO);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    public ResponseEntity<?> getHechosConLatitudYLongitud(Integer origen) {

        System.out.println("ORIGEN: " +  origen);

        List<Hecho> hechosTotales = new ArrayList<>();
        hechosTotales.addAll(hechosEstaticaRepo.findAllByActivoTrueAndLatitudYLongitudNotNull());
        hechosTotales.addAll(hechosDinamicaRepo.findAllByActivoTrueAndLatitudYLongitudNotNull());
        hechosTotales.addAll(hechosProxyRepo.findAllByActivoTrueAndLatitudYLongitudNotNull());

        if (OrigenConexion.fromCodigo(origen).equals(OrigenConexion.FRONT)) {
            List<VisualizarHechosOutputDTO> outputDTO = hechosTotales.stream()
                    .map(hecho -> crearHechoDto(hecho, VisualizarHechosOutputDTO.class))
                    .toList();
            return ResponseEntity.status(HttpStatus.OK).body(outputDTO);
        } else if (OrigenConexion.fromCodigo(origen).equals(OrigenConexion.PROXY)) {
            List<HechoMetamapaResponse> outputDTO = hechosTotales.stream()
                    .map(hecho -> crearHechoDto(hecho, HechoMetamapaResponse.class))
                    .toList();
            return ResponseEntity.status(HttpStatus.OK).body(outputDTO);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private <T> Specification<T> perteneceAColeccionYesConsensuadoSiAplica(List<Long> idsHechosDeColeccionYConsensuados, Class<T> clazz) {
        if (idsHechosDeColeccionYConsensuados == null || idsHechosDeColeccionYConsensuados.isEmpty()) {
            // devuelve false siempre -> WHERE 1=0
            return (root, query, cb) -> cb.disjunction();
        }

        return (root, query, cb) -> root.get("id").in(idsHechosDeColeccionYConsensuados);
    }



    public ResponseEntity<?> addSinonimoPais(Jwt principal, Long idPais, String sinonimo_str) {

        ResponseEntity<?> respuesta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (!respuesta.getStatusCode().equals(HttpStatus.OK)){
            return respuesta;
        }

        Pais pais = repoPais.findById(idPais).orElse(null);

        if(pais == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","El id del pais no es valido"));
        }

        Sinonimo sinonimo = repoSinonimo.findByIdPaisAndNombre(idPais, sinonimo_str).orElse(null);

        if(sinonimo != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El sinonimo ya existe");
        }

        sinonimo = new Sinonimo(sinonimo_str);

        pais.getSinonimos().add(sinonimo);

        repoPais.save(pais);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<?> addSinonimoProvincia(Jwt principal, Long idProvincia, String sinonimo_str) {

        ResponseEntity<?> respuesta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (!respuesta.getStatusCode().equals(HttpStatus.OK)){
            return respuesta;
        }

        Provincia provincia = repoProvincia.findById(idProvincia).orElse(null);

        if(provincia == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","El id de la provincia no es valido"));
        }

        Sinonimo sinonimo = repoSinonimo.findByIdProvinciaAndNombre(idProvincia, sinonimo_str).orElse(null);

        if(sinonimo != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El sinonimo ya existe");
        }

        sinonimo = new Sinonimo(sinonimo_str);

        provincia.getSinonimos().add(sinonimo);

        repoProvincia.save(provincia);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private <T> Specification<T> crearSpecs(List<List<IFiltro>> filtrosXCategoria, Class<T> clazz) {

        System.out.println("ENTRO A crearSpecs");
        System.out.println("Total categorías: " + (filtrosXCategoria != null ? filtrosXCategoria.size() : "null"));

        Specification<T> specFinal = null;

        if (filtrosXCategoria == null || filtrosXCategoria.isEmpty()) {
            System.out.println("La lista de filtros por categoría está vacía o es null.");
            return null;
        }

        for (int i = 0; i < filtrosXCategoria.size(); i++) {
            List<IFiltro> categoria = filtrosXCategoria.get(i);

            if (categoria == null || categoria.isEmpty()) {
                System.out.println("Categoría " + i + " vacía o null, se saltea.");
                continue;
            }

            System.out.println("Procesando categoría " + i + " con " + categoria.size() + " filtros.");

            Specification<T> specCategoria = categoria.stream()
                    .map(f -> {
                        Specification<T> spec = f.toSpecification(clazz);
                        System.out.println("  Filtro: " + f + " -> Spec: " + (spec != null ? "OK" : "null"));
                        return spec;
                    })
                    .filter(Objects::nonNull)
                    .reduce(Specification::or)
                    .orElse(null);

            if (specCategoria == null) {
                System.out.println("No se generó spec para categoría " + i + ".");
                continue;
            }

            specFinal = (specFinal == null) ? specCategoria : specFinal.and(specCategoria);
        }

        System.out.println("Spec final generada: " + (specFinal != null ? "OK" : "null"));

        return specFinal;
    }


    private <T> Specification<T> distinct(Class <T> clazz) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction(); // no agrega condición extra
        };
    }

    public ResponseEntity<?> subirArchivo(MultipartFile file, Long idHecho, Jwt principal){

        HechoDinamica hecho = hechosDinamicaRepo.findById(idHecho).orElse(null);

        if(hecho == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Usuario usuario = usuariosRepo.findByNombreDeUsuario(JwtClaimExtractor.getUsernameFromToken(principal)).orElse(null);
        Long id_usuario = null;
        if(usuario != null){
            id_usuario = usuario.getId();
        }

        if (!Objects.equals(hecho.getUsuario_id(), id_usuario)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        this.guardarContenidoMultimedia(file, hecho);

        hechosDinamicaRepo.save(hecho);

        return ResponseEntity.status(HttpStatus.CREATED).body(hecho.getAtributosHecho().getContenidosMultimedia().get(hecho.getAtributosHecho().getContenidosMultimedia().size() - 1).getId());
    }

    public ResponseEntity<?> getHecho(Long id_hecho, String fuente){
        switch (fuente){
            case "ESTATICA":{
                HechoEstatica hecho = hechosEstaticaRepo.findById(id_hecho).orElse(null);
                hecho.incrementarAccesos();
                hechosEstaticaRepo.save(hecho);
                VisualizarHechosOutputDTO visualizarHechosOutputDTO = crearHechoDto(hecho, VisualizarHechosOutputDTO.class);
                return ResponseEntity.ok(visualizarHechosOutputDTO);
            }
            case "DINAMICA":{
                HechoDinamica hecho = hechosDinamicaRepo.findById(id_hecho).orElse(null);
                hecho.incrementarAccesos();
                hechosDinamicaRepo.save(hecho);
                VisualizarHechosOutputDTO visualizarHechosOutputDTO = crearHechoDto(hecho, VisualizarHechosOutputDTO.class);
                return ResponseEntity.ok(visualizarHechosOutputDTO);
            }
            case "PROXY":{
                HechoProxy hecho = hechosProxyRepo.findById(id_hecho).orElse(null);
                assert hecho != null;
                hecho.incrementarAccesos();
                hechosProxyRepo.save(hecho);
                VisualizarHechosOutputDTO visualizarHechosOutputDTO = crearHechoDto(hecho, VisualizarHechosOutputDTO.class);
                return ResponseEntity.ok(visualizarHechosOutputDTO);
            }
            default: return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public Hecho getHechoEntity(Long id_hecho, String fuente){
        switch (fuente){
            case "ESTATICA":{
                return hechosEstaticaRepo.findById(id_hecho).orElse(null);
            }
            case "DINAMICA":{
                return hechosDinamicaRepo.findById(id_hecho).orElse(null);
            }
            case "PROXY":{
                return hechosProxyRepo.findById(id_hecho).orElse(null);
            }
            default: return null;
        }
    }

    public ResponseEntity<?> getAllPaises(){
        List<Pais> paises = repoPais.findAll();
        List<PaisDto> paisesDtos = new ArrayList<>();

        for (Pais pais : paises){
            paisesDtos.add(PaisDto.builder()
                    .pais(pais.getPais())
                    .id(pais.getId())
                    .build());
        }

        return ResponseEntity.ok().body(paisesDtos);
    }

    public ResponseEntity<?> getAllCategorias(){
        List<Categoria> categorias = categoriaRepository.findAll();
        List<CategoriaDto> categoriasDtos = new ArrayList<>();

        for (Categoria categoria : categorias){
            categoriasDtos.add(CategoriaDto.builder()
                    .categoria(categoria.getTitulo())
                    .id(categoria.getId())
                    .build());
        }

        return ResponseEntity.ok().body(categoriasDtos);
    }

    public ResponseEntity<?> getProvinciasByPais(Long id){
        List<Provincia> provincias = repoProvincia.findAllByPaisId(id);
        List<ProvinciaDto> provinciasDtos = new ArrayList<>();

        for (Provincia provincia : provincias){
            provinciasDtos.add(ProvinciaDto.builder()
                    .provincia(provincia.getProvincia())
                    .id(provincia.getId())
                    .build());
        }

        return ResponseEntity.ok().body(provinciasDtos);
    }


    public ResponseEntity<Long> getCantHechos() {
        Long cantidadHechos = 0L;

        cantidadHechos += hechosDinamicaRepo.getCantHechos();
        cantidadHechos += hechosEstaticaRepo.getCantHechos();
        cantidadHechos += hechosProxyRepo.getCantHechos();

        return ResponseEntity.ok(cantidadHechos);
    }

    public ResponseEntity<?> getPaisProvincia(Double latitud, Double longitud){
        UbicacionString ubicacionString = Geocodificador.obtenerUbicacion(latitud, longitud);
        if (ubicacionString != null){
            PaisProvinciaDTO paisProvinciaDTO = new PaisProvinciaDTO();
            String paisStr = ubicacionString.getPais();
            System.out.println("SOY UN PAIS MUY FELIZ, Y ME LLAMO: " + paisStr);
            Pais pais = null;
            if (paisStr != null){
                pais = repoPais.findByNombreNormalizado(paisStr).orElse(null);
                if (pais != null){
                    PaisDto paisDto = PaisDto.builder().pais(paisStr).id(pais.getId()).build();
                    paisProvinciaDTO.setPaisDto(paisDto);
                }
            }
            if (pais!=null){
                String provinciaStr = ubicacionString.getProvincia();
                if (provinciaStr != null){
                    System.out.println("SOY UNA PROVINCIA FELIZ, Y ME LLAMO: " + provinciaStr);
                    Provincia provincia = repoProvincia.findByNombreNormalizadoAndPaisId(provinciaStr, pais.getId()).orElse(null);
                    if (provincia != null){
                        System.out.println("SIUU NO SOY PROVINCIA NULL Y ME LLAMO: " + provincia.getProvincia());
                        ProvinciaDto provinciaDto = ProvinciaDto.builder().provincia(provinciaStr).id(provincia.getId()).build();
                        paisProvinciaDTO.setProvinciaDto(provinciaDto);
                    }
                    else{
                        System.out.println("SOY UNA PROVINCIA DE TITULO: " + provincia.getProvincia());
                    }
                }
            }


            return ResponseEntity.ok(paisProvinciaDTO);
        }
        return ResponseEntity.ok().build(); // Si bien sería un not found, envío esto para evitar problemas con el retrieve

    }

    public ResponseEntity<?> getHechosDelUsuario(String username){

        if (username == null || username.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre de usuario es requerido.");
        }

        Usuario usuario = usuariosRepo.findByNombreDeUsuario(username).orElse(null);

        if (usuario == null){
            // Si el token es válido, esto no debería suceder, pero es un buen chequeo de seguridad.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Long usuarioId = usuario.getId();

        List<HechoDinamica> misHechosDinamicos = hechosDinamicaRepo.findAllByUsuarioIdAndActivoTrue(usuarioId);

        List<VisualizarHechosOutputDTO> outputDTO = misHechosDinamicos.stream()
                .map(hecho -> crearHechoDto(hecho, VisualizarHechosOutputDTO.class))
                .toList();

        System.out.println("Hechos encontrados para " + username + ": " + outputDTO.size());

        return ResponseEntity.status(HttpStatus.OK).body(outputDTO);
    }

    public ResponseEntity<?> getHechosDestacados() {
        List<HechoEstatica> hechosEstatica = hechosEstaticaRepo.findHechosDestacados();
        List<HechoDinamica> hechosDinamica = hechosDinamicaRepo.findHechosDestacados();
        List<HechoProxy> hechosProxy = hechosProxyRepo.findHechosDestacados();

        List<Hecho> top3Hechos = Stream.of(hechosEstatica, hechosDinamica, hechosProxy)
                .flatMap(list -> list.stream().map(h -> (Hecho) h))  // 👈 conversión explícita
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(Hecho::getCant_accesos).reversed())
                .limit(3)
                .toList();
        // Devuelve lista inmutable

        List<VisualizarHechosOutputDTO> hechosDto = new ArrayList<>();

        for(Hecho hecho : top3Hechos){
            VisualizarHechosOutputDTO hechoDto = this.crearHechoDto(hecho, VisualizarHechosOutputDTO.class);
            hechosDto.add(hechoDto);
        }

        return ResponseEntity.status(HttpStatus.OK).body(hechosDto);
    }

    @Transactional
    public ResponseEntity<?> eliminarHecho(Long id, String fuente, String username) {

        ResponseEntity<?> rta = checkeoAdmin(username);

        RolCambiadoDTO dto = new RolCambiadoDTO();
        dto.setRolModificado(false);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }

        Hecho hecho = this.getHechoEntity(id, fuente);

        if (hecho == null){
            return ResponseEntity.notFound().build();
        }

        hecho.setActivo(false);
        hecho.getAtributosHecho().setModificado(true);
        if (hecho instanceof HechoEstatica){
            hechosEstaticaRepo.save((HechoEstatica) hecho);
        }

        if (hecho instanceof HechoDinamica){
            hechosDinamicaRepo.save((HechoDinamica) hecho);
        }

        if (hecho instanceof HechoProxy){
            hechosProxyRepo.save((HechoProxy) hecho);
        }

        if (hecho.getUsuario_id() != null) {

            Usuario usuario = usuariosRepo.findById(hecho.getUsuario_id()).orElse(null);

            if (usuario != null) {
                usuario.disminuirHechosSubidos();
                Mensaje mensaje = new Mensaje();
                mensaje.setTextoMensaje("Se eliminó su hecho de título " + hecho.getAtributosHecho().getTitulo());
                mensaje.setReceptor(usuario);
                mensajeRepository.save(mensaje);
                if (usuario.getCantHechosSubidos() == 0 && usuario.getRol().equals(Rol.CONTRIBUYENTE)){
                    dto.setRolModificado(true);
                    dto.setRol(Rol.VISUALIZADOR);
                    dto.setUsername(usuario.getNombreDeUsuario());
                    GestorRoles.ContribuyenteAVisualizador(usuario);
                }
            }

        }
        return ResponseEntity.ok().body(dto);
    }

    @Transactional
    public ResponseEntity<?> modificarHecho(HechoModificarInputDTO dto, String username) {

        System.out.println("LLEGO ACÁ");

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (rta.getStatusCode().equals(HttpStatus.FORBIDDEN)){
            return rta;
        }

        Hecho hecho = this.getHechoEntity(dto.getId_hecho(), dto.getFuente());

        if (hecho == null){
            return ResponseEntity.notFound().build();
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

        this.setearModificadoAOficial(hecho, atributos);
        hecho.getAtributosHecho().setFechaUltimaActualizacion(LocalDateTime.now());
        hecho.getAtributosHecho().setModificado(true);

        if (hecho instanceof HechoEstatica){
            hechosEstaticaRepo.save((HechoEstatica) hecho);
        }
        else if (hecho instanceof HechoDinamica){
            hechosDinamicaRepo.save((HechoDinamica) hecho);
        }
        else if (hecho instanceof HechoProxy){
            hechosProxyRepo.save((HechoProxy) hecho);
        }

        if (hecho.getUsuario_id() != null){
            Usuario usuario = usuariosRepo.findById(hecho.getUsuario_id()).orElse(null);

            if (usuario != null) {
                Mensaje mensaje = new Mensaje();
                mensaje.setTextoMensaje("Se modificó su hecho de título " + hecho.getAtributosHecho().getTitulo());
                mensaje.setReceptor(usuario);
                mensajeRepository.save(mensaje);
            }
        }


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


    public ResponseEntity<?> getCantFuentes(String username) {
        ResponseEntity<?> rta = this.checkeoAdmin(username);
        if(!rta.getStatusCode().is2xxSuccessful()){
            return rta;
        } else {
            return ResponseEntity.ok().body(repoFuentes.getCantFuentes());
        }
    }

    public ResponseEntity<?> getContenidoMultimediaHecho(Long id_hecho, String fuente){
        Hecho hecho = this.getHechoEntity(id_hecho, fuente);
        if (hecho!=null){
            List<ContenidoMultimedia> contenidoMultimedia = hecho.getAtributosHecho().getContenidosMultimedia();
            if (contenidoMultimedia != null && !contenidoMultimedia.isEmpty()){
                return ResponseEntity.ok().body(hecho.getAtributosHecho().getContenidosMultimedia());
            }

        }
        return ResponseEntity.ok().build(); // No mando error pero no mando body. Se encarga el back de no rellenar contenido multimedia
    }


}
