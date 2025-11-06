package modulos.agregacion.services;

import io.jsonwebtoken.Jwt;
import modulos.JwtClaimExtractor;
import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.agregacion.entities.atributosHecho.ContenidoMultimedia;
import modulos.agregacion.entities.DbMain.filtros.*;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import modulos.agregacion.entities.HechoMemoria;
import modulos.agregacion.entities.atributosHecho.OrigenConexion;
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
import modulos.agregacion.entities.atributosHecho.AtributosHecho;
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
import java.util.UUID;
import java.time.ZonedDateTime;
import java.util.*;

@Service
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
    private FormateadorHechoMemoria formateadorHechoMemoria;

    public HechosService(IHechosEstaticaRepository hechosEstaticaRepo,
                         IHechosDinamicaRepository hechosDinamicaRepo,
                         IHechosProxyRepository hechosProxyRepo,
                         IUsuarioRepository usuariosRepo,
                         IColeccionRepository coleccionRepo,
                         IDatasetsRepository datasetsRepo,
                         ICategoriaRepository categoriaRepository,
                         IProvinciaRepository repoProvincia,
                         IPaisRepository repoPais,
                         BuscadoresRegistry buscadores, ISinonimoRepository repoSinonimo,
                         IHechoRefRepository hechoRefRepository,
                         FormateadorHechoMemoria formateadorHechoMemoria){
        this.repoProvincia = repoProvincia;
        this.repoPais = repoPais;
        this.hechosDinamicaRepo = hechosDinamicaRepo;
        this.hechosEstaticaRepo = hechosEstaticaRepo;
        this.hechosProxyRepo = hechosProxyRepo;
        this.usuariosRepo = usuariosRepo;
        this.coleccionRepo = coleccionRepo;
        this.datasetsRepo = datasetsRepo;
        this.categoriaRepository = categoriaRepository;
        this.repoSinonimo = repoSinonimo;
        this.buscadores = buscadores;
        this.hechoRefRepository = hechoRefRepository;
        this.formateadorHechoMemoria = formateadorHechoMemoria;
    }

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(usuario);
    }


    //lo sube un administrador (lo considero carga dinamica)
    @Transactional
    public ResponseEntity<?> subirHecho(SolicitudHechoInputDTO dtoInput, String username){

        if(dtoInput.getTitulo() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (!rta.getStatusCode().equals(HttpStatus.OK)){
            return rta;
        }

        Usuario usuario = (Usuario)rta.getBody();
        usuario.incrementarHechosSubidos();

        HechoDinamica hecho = new HechoDinamica();

        System.out.println("SOY UN ID PAIS CONTENTO: " + dtoInput.getId_pais());
        AtributosHecho atributos = FormateadorHecho.formatearAtributosHecho(buscadores, dtoInput);

        hecho.setUsuario_id(usuario.getId());
        hecho.setAtributosHecho(atributos);
        hecho.setActivo(true);
        hecho.getAtributosHecho().setModificado(true);
        hecho.getAtributosHecho().setFuente(Fuente.DINAMICA);
        ZonedDateTime fecha = ZonedDateTime.now();
        System.out.printf("FECHA:" + fecha);
        hecho.getAtributosHecho().setFechaCarga(fecha);
        System.out.println("FECHA:" + fecha);
        hecho.getAtributosHecho().setFechaUltimaActualizacion(hecho.getAtributosHecho().getFechaCarga());

        if (dtoInput.getContenidosMultimedia() != null){
            for(MultipartFile contenidoMultimedia : dtoInput.getContenidosMultimedia()){
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
        } catch (IOException ignored) {
        }
    }

    @Transactional
    public ResponseEntity<?> importarHechos(ImportacionHechosInputDTO dtoInput, MultipartFile file, Jwt principal) {
        try {
            ResponseEntity<?> rta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

            if (!rta.getStatusCode().equals(HttpStatus.OK)) {
                return rta;
            }

            // 1) Validaciones básicas
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Debés adjuntar un archivo CSV");
            }
            if (!Objects.equals(file.getContentType(), "text/csv") && !Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("El archivo debe ser CSV");
            }

            // === Opción A: guardar en disco ===
            Path base = Paths.get("uploads/datasets").toAbsolutePath().normalize();
            Files.createDirectories(base);
            String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destino = base.resolve(storedName);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);


            FuenteEstatica fuente = new FuenteEstatica();
            Dataset dataset = new Dataset(dtoInput.getFuenteString());
            dataset.setStoragePath(destino.toString());
            datasetsRepo.save(dataset);
            fuente.setDataSet(dataset);

            List<HechoEstatica> hechos = fuente.leerFuente((Usuario) rta.getBody(), buscadores);

            for (HechoEstatica hecho : hechos) {
                hecho.setActivo(true);
                ZonedDateTime fechaActual = ZonedDateTime.now();
                hecho.getAtributosHecho().setFechaCarga(fechaActual);
                hecho.getAtributosHecho().setFechaUltimaActualizacion(fechaActual);
                hechosEstaticaRepo.save(hecho);
                hechoRefRepository.save(new HechoRef(hecho.getId(), hecho.getAtributosHecho().getFuente()));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Se importaron los hechos correctamente");
        } catch (IOException io) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo debe ser CSV");
        }
    }


    public ResponseEntity<?> getHechosColeccion(GetHechosColeccionInputDTO inputDTO){
        // TODO: Criterio de fuente

        CriteriosColeccionDTO criterios;

        if(OrigenConexion.fromCodigo(inputDTO.getOrigenConexion()).equals(OrigenConexion.FRONT)) {
            criterios = CriteriosColeccionDTO.builder()
                    .categoriaId(inputDTO.getCategoriaId())
                    .contenidoMultimedia(inputDTO.getContenidoMultimedia())
                    .descripcion(inputDTO.getDescripcion())
                    .fechaAcontecimientoInicial(inputDTO.getFechaAcontecimientoInicial())
                    .fechaAcontecimientoFinal(inputDTO.getFechaAcontecimientoFinal())
                    .fechaCargaInicial(inputDTO.getFechaCargaInicial())
                    .fechaCargaFinal(inputDTO.getFechaCargaFinal())
                    .origen(inputDTO.getOrigen())
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
                    .origen(inputDTO.getOrigen())
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
                } else if (filtro instanceof FiltroOrigen fo) {
                    System.out.println("  [FiltroOrigen] origen=" + fo.getOrigenDeseado());
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
        if(inputDTO.getNavegacionCurada()) {

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

            hechosFiltrados.addAll(hechosFiltradosDinamica);
            hechosFiltrados.addAll(hechosFiltradosProxy);

        }

        if (OrigenConexion.fromCodigo(inputDTO.getOrigenConexion()).equals(OrigenConexion.FRONT)) {
            System.out.println("VOY A MAPEAR HECHOS A VISUALIZARHECHOSOUTPUTDTO");
            List<VisualizarHechosOutputDTO> outputDTO = hechosFiltrados.stream()
                    .map(hecho -> crearHechoDto(hecho, VisualizarHechosOutputDTO.class))
                    .toList();

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

    public ResponseEntity<?> addCategoria(Jwt principal, String categoriaStr, List<String> sinonimosString) {

        ResponseEntity<?> rta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (!rta.getStatusCode().equals(HttpStatus.OK)){
            return rta;
        }
        Categoria categoria = new Categoria();
        categoria.setTitulo(categoriaStr);
        List<Sinonimo> sinonimos = new ArrayList<>();
        if (sinonimosString!=null && !sinonimosString.isEmpty()){
            for (String sinonimo: sinonimosString){
                sinonimos.add(new Sinonimo(sinonimo));
            }
        }
        categoria.setSinonimos(sinonimos);
        categoriaRepository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body("Se creó la categoría correctamente");
    }

    public ResponseEntity<?> addSinonimoCategoria(Jwt principal, Long idCategoria, String sinonimo_str) {

        ResponseEntity<?> respuesta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (!respuesta.getStatusCode().equals(HttpStatus.OK)){
            return respuesta;
        }

    Categoria categoria = categoriaRepository.findById(idCategoria).orElse(null);

        if(categoria == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","El id de la categoria no es valido"));
        }

        Sinonimo sinonimo = repoSinonimo.findByIdCategoriaAndNombre(idCategoria, sinonimo_str).orElse(null);

        if(sinonimo != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El sinonimo ya existe");
        }

        sinonimo = new Sinonimo(sinonimo_str);

        categoria.getSinonimos().add(sinonimo);

        categoriaRepository.save(categoria);

        return ResponseEntity.status(HttpStatus.CREATED).build();
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        this.guardarContenidoMultimedia(file, hecho);

        hechosDinamicaRepo.save(hecho);

        return ResponseEntity.status(HttpStatus.CREATED).body(hecho.getAtributosHecho().getContenidosMultimedia().get(hecho.getAtributosHecho().getContenidosMultimedia().size() - 1).getId());
    }

    public ResponseEntity<?> getHecho(Long id_hecho, String fuente){
        switch (fuente){
            case "ESTATICA":{
                HechoEstatica hecho = hechosEstaticaRepo.findById(id_hecho).orElse(null);
                VisualizarHechosOutputDTO visualizarHechosOutputDTO = crearHechoDto(hecho, VisualizarHechosOutputDTO.class);
                return ResponseEntity.ok(visualizarHechosOutputDTO);
            }
            case "DINAMICA":{
                HechoDinamica hecho = hechosDinamicaRepo.findById(id_hecho).orElse(null);
                VisualizarHechosOutputDTO visualizarHechosOutputDTO = crearHechoDto(hecho, VisualizarHechosOutputDTO.class);
                return ResponseEntity.ok(visualizarHechosOutputDTO);
            }
            case "PROXY":{
                HechoProxy hecho = hechosProxyRepo.findById(id_hecho).orElse(null);
                VisualizarHechosOutputDTO visualizarHechosOutputDTO = crearHechoDto(hecho, VisualizarHechosOutputDTO.class);
                return ResponseEntity.ok(visualizarHechosOutputDTO);
            }
            default: return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
            if (paisStr != null){
                Pais pais = repoPais.findByNombreNormalizado(paisStr).orElse(null);
                if (pais != null){
                    PaisDto paisDto = PaisDto.builder().pais(paisStr).id(pais.getId()).build();
                    paisProvinciaDTO.setPaisDto(paisDto);
                }
            }
            String provinciaStr = ubicacionString.getProvincia();
            if (provinciaStr != null){
                Provincia provincia = repoProvincia.findByNombreNormalizado(provinciaStr).orElse(null);
                if (provincia != null){
                    ProvinciaDto provinciaDto = ProvinciaDto.builder().provincia(provinciaStr).id(provincia.getId()).build();
                    paisProvinciaDTO.setProvinciaDto(provinciaDto);
                }
            }

            return ResponseEntity.ok(paisProvinciaDTO);
        }
        return ResponseEntity.ok().build(); // Si bien sería un not found, envío esto para evitar problemas con el retrieve

    }
}
