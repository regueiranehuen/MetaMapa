package modulos.agregacion.services;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import modulos.JwtClaimExtractor;
import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbMain.algoritmosConsenso.AlgoritmoConsensoMayoriaAbsoluta;
import modulos.agregacion.entities.DbMain.algoritmosConsenso.AlgoritmoConsensoMayoriaSimple;
import modulos.agregacion.entities.DbMain.algoritmosConsenso.AlgoritmoConsensoMultiplesMenciones;
import modulos.agregacion.entities.DbMain.filtros.*;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import modulos.agregacion.repositories.DbDinamica.IHechosDinamicaRepository;
import modulos.agregacion.repositories.DbEstatica.IDatasetsRepository;
import modulos.agregacion.repositories.DbEstatica.IHechosEstaticaRepository;
import modulos.agregacion.repositories.DbMain.IColeccionRepository;
import modulos.agregacion.repositories.DbMain.IFiltroRepository;
import modulos.agregacion.repositories.DbMain.IHechoRefRepository;
import modulos.agregacion.repositories.DbMain.IUsuarioRepository;
import modulos.agregacion.repositories.DbProxy.IHechosProxyRepository;
import modulos.shared.utils.FormateadorHecho;
import modulos.agregacion.entities.DbEstatica.Dataset;
import modulos.agregacion.entities.fuentes.FuenteEstatica;
import modulos.buscadores.*;
import modulos.shared.dtos.input.*;
import modulos.agregacion.entities.DbMain.usuario.Rol;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import modulos.shared.dtos.output.ColeccionOutputDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static modulos.JwtClaimExtractor.getUsernameFromToken;

@Service
public class ColeccionService  {

    private final IColeccionRepository coleccionesRepo;
    private final IUsuarioRepository usuariosRepo;
    private final IDatasetsRepository datasetsRepo;
    private final BuscadoresRegistry buscadores;
    private final IHechosEstaticaRepository hechosEstaticaRepository;
    private final IHechosDinamicaRepository hechosDinamicaRepository;
    private final IHechosProxyRepository hechosProxyRepository;
    private final IHechoRefRepository hechoRefRepository;
    private final IFiltroRepository filtroRepository;

    public ColeccionService(IColeccionRepository coleccionesRepo,
                            IUsuarioRepository usuariosRepo,
                            IDatasetsRepository datasetsRepo,
                            IHechosEstaticaRepository hechosEstaticaRepository,
                            IHechosDinamicaRepository hechosDinamicaRepository,
                            IHechosProxyRepository hechosProxyRepository,
                            BuscadoresRegistry buscadores,
                            IHechoRefRepository hechoRefRepository,
                            IDatasetsRepository datasetsRepository,
                            IFiltroRepository filtroRepository) {
        this.coleccionesRepo = coleccionesRepo;
        this.usuariosRepo = usuariosRepo;
        this.datasetsRepo = datasetsRepo;
        this.buscadores = buscadores;
        this.hechosDinamicaRepository = hechosDinamicaRepository;
        this.hechosEstaticaRepository = hechosEstaticaRepository;
        this.hechosProxyRepository = hechosProxyRepository;
        this.hechoRefRepository = hechoRefRepository;
        this.filtroRepository = filtroRepository;
    }


    /*
    Colecciones
Las colecciones representan conjuntos de hechos. Las mismas pueden ser consultadas por cualquier persona, de forma
pública, y no pueden ser editadas ni eliminadas manualmente (esto último, con una sola excepción, ver más adelante).

Las colecciones tienen un título, como por ejemplo “Desapariciones vinculadas a crímenes de odio”, o “Incendios
forestales en Argentina 2025” y una descripción. Las personas administradoras pueden crear tantas colecciones como deseen.

Las colecciones están asociadas a una fuente y tomarán los hechos de las mismas: para esto las colecciones también contarán con un criterio de
pertenencia configurable, que dictará si un hecho pertenece o no a las mismas. Por ejemplo, la colección de “Incendios forestales…” deberá
incluir automáticamente todos los hechos de categoría “Incendio forestal” ocurrido en Argentina, acontecido entre el 1 de enero de 2025 a las
0:00 y el 31 de diciembre de 20205 a las 23:59.

    */

    @Transactional
    public ResponseEntity<?> crearColeccion(ColeccionInputDTO dtoInput, String username) {


        ResponseEntity<?> rta = checkeoAdmin(username);

        if (!rta.getStatusCode().is2xxSuccessful()){
            return rta;
        }

        DatosColeccion datosColeccion = new DatosColeccion(dtoInput.getTitulo(), dtoInput.getDescripcion());

        Coleccion coleccion = new Coleccion(datosColeccion);

        coleccion.setActivo(true);
        coleccion.setModificado(false);

//todo endpoint get all algoritmo consenso con (nombre)
        if (dtoInput.getAlgoritmoConsenso() != null){
            switch (dtoInput.getAlgoritmoConsenso()) {
                case "MAYORIA_ABSOLUTA":
                        coleccion.setAlgoritmoConsenso(new AlgoritmoConsensoMayoriaAbsoluta());
                        break;
                case "MAYORIA_SIMPLE":
                    coleccion.setAlgoritmoConsenso(new AlgoritmoConsensoMayoriaSimple());
                    break;
                case "MULTIPLES_MENCIONES":
                        coleccion.setAlgoritmoConsenso(new AlgoritmoConsensoMultiplesMenciones());
                        break;
                default:
                    break;
            }
        }

        List<List<IFiltro>> filtros = FormateadorHecho.obtenerListaDeFiltros(FormateadorHecho.formatearFiltrosColeccionDinamica(buscadores, dtoInput.getCriterios()));

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

        List<Filtro> filtrosJuntos = filtros.stream()
                .flatMap(List::stream)     // aplana las sublistas
                .map(f -> (Filtro) f)      // castea cada elemento individual
                .collect(Collectors.toCollection(ArrayList::new)); // mutable ✅
        coleccion.setCriterios(filtrosJuntos);


        coleccionesRepo.saveAndFlush(coleccion);
        return ResponseEntity.status(HttpStatus.CREATED).body("La colección se creó correctamente");
    }

    public ResponseEntity<?> obtenerTodasLasColecciones(){

        List<ColeccionOutputDTO> listaDTO = new ArrayList<>();

        List<Coleccion> colecciones = coleccionesRepo.findAllByActivoTrue();

        for (Coleccion coleccion : colecciones){
            ColeccionOutputDTO dto = new ColeccionOutputDTO();
            dto.setId(coleccion.getId());
            dto.setTitulo(coleccion.getTitulo());
            dto.setDescripcion(coleccion.getDescripcion());
            dto.setCriterios(FormateadorHecho.filtrosColeccionToString(coleccion.getCriterios()));
            listaDTO.add(dto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(listaDTO);
    }

    public ResponseEntity<?> getColeccion(Long id_coleccion) {

        Coleccion coleccion = coleccionesRepo.findByIdAndActivoTrue(id_coleccion).orElse(null);

        if(coleccion == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró la colección");
        }

        ColeccionOutputDTO dto = new ColeccionOutputDTO();

        dto.setId(coleccion.getId());
        dto.setTitulo(coleccion.getTitulo());
        dto.setDescripcion(coleccion.getDescripcion());

        dto.setCriterios(FormateadorHecho.filtrosColeccionToString(coleccion.getCriterios()));

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    public ResponseEntity<?> deleteColeccion(Long id_coleccion, Jwt principal) {

        ResponseEntity<?> rta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (!rta.getStatusCode().is2xxSuccessful()){
            return rta;
        }

        Coleccion coleccion = coleccionesRepo.findByIdAndActivoTrue(id_coleccion).orElse(null);
        if(coleccion == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró la colección");
        }
        coleccion.setActivo(false);

        coleccionesRepo.save(coleccion);

        return ResponseEntity.status(HttpStatus.OK).body("Se ha borrado la colección");
    }


    public ResponseEntity<?> agregarFuente(Long idColeccion, String dataSet, Jwt principal) {

        ResponseEntity<?> respuesta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if(!respuesta.getStatusCode().equals(HttpStatus.OK)){
            return respuesta;
        }

        Coleccion coleccion = coleccionesRepo.findByIdAndActivoTrue(idColeccion).orElse(null);

        if(coleccion == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        FuenteEstatica fuente = new FuenteEstatica();
        Dataset dataset = new Dataset(dataSet);
        fuente.setDataSet(dataset);

        List<HechoEstatica> hechosFuente = fuente.leerFuente((Usuario)respuesta.getBody(), buscadores);
        List<HechoRef> hechosRef = new ArrayList<>();
        for (HechoEstatica h : hechosFuente){
            hechosRef.add(new HechoRef(h.getId(), Fuente.ESTATICA));
        }

        coleccion.addHechos(hechosRef);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /*
    * Muy buena lectura 👌
Sí, lo que el enunciado te está pidiendo indirectamente es eso:

🔹 Cuando agregás una fuente a una colección

Esa colección ahora “escucha” también a esa fuente/dataset.

Por lo tanto, todos los hechos de esa fuente que cumplan los criterios de la colección deben incorporarse.

Técnicamente:

Asociás la fuente a la colección.

Leés todos los hechos de esa fuente.

Filtrás por los criterios de la colección (categoría, fechas, etc.).

Los agregás a la colección.

🔹 Cuando quitás una fuente de una colección

Dejás de usar esa fuente como input de hechos.

Por lo tanto, todos los hechos que provienen de esa fuente deben eliminarse de la colección (o al menos dejar de estar vinculados).

Esto asegura que la colección refleje solo los hechos de las fuentes actualmente asociadas*/

    public ResponseEntity<?> eliminarFuente(Long idColeccion, Long id_dataset, Jwt principal) {

        ResponseEntity<?> respuesta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if(respuesta.getStatusCode().equals(HttpStatus.UNAUTHORIZED)){
            return respuesta;
        }

        Coleccion coleccion = coleccionesRepo.findByIdAndActivoTrue(idColeccion).orElse(null);

        Dataset dataset = datasetsRepo.findById(id_dataset).orElse(null);

        if(dataset == null || coleccion == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }


        List<Long> idsHechosEstaticaFuente = hechosEstaticaRepository.findHechosByDataset(id_dataset).stream().map(Hecho::getId).toList();
        List<HechoRef> hechosEstaticaRef = hechoRefRepository.findByFuente(Fuente.ESTATICA.codigoEnString());

        List<HechoRef> hechosFiltrados = new ArrayList<>();

        for (HechoRef hr : hechosEstaticaRef){
            if (idsHechosEstaticaFuente.contains(hr.getKey().getId())){
                hechosFiltrados.add(hr);
            }
        }

        coleccion.getHechos().removeAll(hechosFiltrados);

        coleccionesRepo.save(coleccion);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public ResponseEntity<?> updateColeccion(ColeccionUpdateInputDTO dto, Jwt principal) {

        ResponseEntity<?> respuesta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if(!respuesta.getStatusCode().equals(HttpStatus.OK)){
            return respuesta;
        }

        Coleccion coleccion = coleccionesRepo.findByIdAndActivoTrue(dto.getId_coleccion()).orElse(null);

        if (coleccion == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body("No se encontró la colección");
        }

        if (dto.getTitulo() != null){
            coleccion.setTitulo(dto.getTitulo());
        }

        if (dto.getDescripcion() != null){
            coleccion.setDescripcion(dto.getDescripcion());
        }

        List<List<IFiltro>> filtros = FormateadorHecho.obtenerListaDeFiltros(FormateadorHecho.formatearFiltrosColeccion(buscadores, dto.getCriterios()));
        List<Filtro> filtrosJuntos = new ArrayList<>();
        filtros.forEach(f -> filtrosJuntos.add((Filtro) f));
        coleccion.setCriterios(filtrosJuntos);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void ejecutarAlgoritmoConsenso(){
        List<Coleccion> colecciones = coleccionesRepo.findAllByActivoTrue();
        List<Dataset> datasets = datasetsRepo.findAll();
        colecciones.forEach(coleccion->coleccion.getAlgoritmoConsenso().ejecutarAlgoritmoConsenso(buscadores.getBuscadorHecho(), datasets, coleccion));
    }

    public ResponseEntity<?> modificarAlgoritmoConsenso(ModificarConsensoInputDTO input, Jwt principal) {

        ResponseEntity<?> rta = checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (!rta.getStatusCode().equals(HttpStatus.OK)){
            return rta;
        }

        Coleccion coleccion = coleccionesRepo.findByIdAndActivoTrue(input.getIdColeccion()).orElse(null);
        if (coleccion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró la colección");
        }

        //no habría que recibir el id del algoritmo?

        if (input.getTipoConsenso() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        switch (input.getTipoConsenso()) {
            case "AlgoritmoConsensoMayoriaAbsoluta":
                coleccion.setAlgoritmoConsenso(new AlgoritmoConsensoMayoriaAbsoluta());
                break;
            case "AlgoritmoConsensoMayoriaSimple":
                coleccion.setAlgoritmoConsenso(new AlgoritmoConsensoMayoriaSimple());
                break;
            case "AlgoritmoConsensoMultiplesMenciones":
                coleccion.setAlgoritmoConsenso(new AlgoritmoConsensoMultiplesMenciones());
                break;
            default:
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El algoritmo de consenso especificado no existe");
        }
        coleccionesRepo.save(coleccion);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Async
    @Scheduled(cron = "0 0 * * * *") // cada hora
    public void refrescarColeccionesCronjob() {
        Specification<HechoEstatica> specs1 = (root, query, cb) -> {
            if (query != null) query.distinct(true); // útil si después hay JOINs
            // activo = true AND atributosHecho.modificado = true (null => false)
            var activo = root.<Boolean>get("activo");
            var modif  = root.get("atributosHecho").<Boolean>get("modificado");
            return cb.and(cb.isTrue(activo), cb.isTrue(cb.coalesce(modif, cb.literal(false))));
        };

        Specification<HechoDinamica> specs2 = (root, query, cb) -> {
            if (query != null) query.distinct(true); // útil si después hay JOINs
            // activo = true AND atributosHecho.modificado = true (null => false)
            var activo = root.<Boolean>get("activo");
            var modif  = root.get("atributosHecho").<Boolean>get("modificado");
            return cb.and(cb.isTrue(activo), cb.isTrue(cb.coalesce(modif, cb.literal(false))));
        };

        Specification<HechoProxy> specs3 = (root, query, cb) -> {
            if (query != null) query.distinct(true); // útil si después hay JOINs
            // activo = true AND atributosHecho.modificado = true (null => false)
            var activo = root.<Boolean>get("activo");
            var modif  = root.get("atributosHecho").<Boolean>get("modificado");
            return cb.and(cb.isTrue(activo), cb.isTrue(cb.coalesce(modif, cb.literal(false))));
        };


        List<Coleccion> colecciones = coleccionesRepo.findByActivoTrue();

        for(Coleccion coleccion : colecciones){
            Specification<HechoEstatica> specsEstatica = crearSpecs(coleccion.getCriterios(), HechoEstatica.class);
            Specification<HechoDinamica> specsDinamica = crearSpecs(coleccion.getCriterios(), HechoDinamica.class);
            Specification<HechoProxy> specsProxy = crearSpecs(coleccion.getCriterios(), HechoProxy.class);

            Specification<HechoEstatica> specFinalEstatica = Specification
                    .where(this.distinct(HechoEstatica.class))
                    .and(specs1)
                    .and(specsEstatica);

            Specification<HechoDinamica> specFinalDinamica = Specification
                    .where(this.distinct(HechoDinamica.class))
                    .and(specs2)
                    .and(specsDinamica);

            Specification<HechoProxy> specFinalProxy = Specification
                    .where(this.distinct(HechoProxy.class))
                    .and(specs3)
                    .and(specsProxy);

            List<HechoEstatica> hechosEstatica = hechosEstaticaRepository.findAll(specFinalEstatica);
            List<HechoDinamica> hechosDinamica = hechosDinamicaRepository.findAll(specFinalDinamica);
            List<HechoProxy> hechosProxy = hechosProxyRepository.findAll(specFinalProxy);

            List<Hecho> hechosFiltrados = new ArrayList<>();
            hechosFiltrados.addAll(hechosEstatica);
            hechosFiltrados.addAll(hechosDinamica);
            hechosFiltrados.addAll(hechosProxy);

            hechosFiltrados.forEach(hecho -> hecho.getAtributosHecho().setModificado(false));

            if(!hechosFiltrados.isEmpty()) {
                coleccion.setModificado(false);
                coleccion.setHechos(hechosFiltrados.stream().map(h->new HechoRef(h.getId(), h.getAtributosHecho().getFuente())).toList());
                coleccionesRepo.save(coleccion);
            }
        }
    }

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

    public ResponseEntity<?> refrescarColecciones(Jwt principal){

        ResponseEntity<?> respuesta = this.checkeoAdmin(JwtClaimExtractor.getUsernameFromToken(principal));

        if (respuesta.getStatusCode().equals(HttpStatus.UNAUTHORIZED)){
            return respuesta;
        }
        this.refrescarColeccionesCronjob();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private <T> Specification<T> crearSpecs(List<Filtro> filtros, Class<T> clazz) {
        return filtros.stream()
                .map(filtro->filtro.toSpecification(clazz))  // o IFiltro::toSpecification
                .filter(Objects::nonNull)
                .reduce(Specification.where(null), Specification::and); // NO meter distinct acá
    }

    private <T> Specification<T> distinct(Class <T> clazz) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction(); // no agrega condición extra
        };
    }

    public ResponseEntity<?> getCantColecciones() {
    return ResponseEntity.ok(coleccionesRepo.cantColecciones());
    }
}

