package modulos.apis;

import jakarta.transaction.Transactional;
import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.agregacion.entities.DbMain.projections.*;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import modulos.agregacion.repositories.DbDinamica.IHechosDinamicaRepository;
import modulos.agregacion.repositories.DbEstatica.IHechosEstaticaRepository;
import modulos.agregacion.repositories.DbMain.ICategoriaRepository;
import modulos.agregacion.repositories.DbMain.IColeccionRepository;
import modulos.agregacion.repositories.DbMain.IProvinciaRepository;
import modulos.agregacion.repositories.DbDinamica.ISolicitudEliminarHechoRepository;
import modulos.agregacion.repositories.DbMain.IUbicacionRepository;
import modulos.agregacion.repositories.DbProxy.IHechosProxyRepository;
import modulos.servicioEstadistica.entities.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DatosQuery implements IDatosQuery{

    IColeccionRepository repoColeccion;
    IProvinciaRepository repoProvincia;
    ICategoriaRepository repoCategoria;
    ISolicitudEliminarHechoRepository repoSoliElimHecho;
    IHechosDinamicaRepository hechosDinamicaRepository;
    IHechosEstaticaRepository hechosEstaticaRepository;
    IHechosProxyRepository hechosProxyRepository;
    IUbicacionRepository ubicacionRepository;

    public DatosQuery(IUbicacionRepository ubicacionRepository, IColeccionRepository repoColeccion, IProvinciaRepository repoProvincia, ICategoriaRepository repoCategoria, ISolicitudEliminarHechoRepository repoSoliElimHecho, IHechosDinamicaRepository hechosDinamicaRepository, IHechosEstaticaRepository hechosEstaticaRepository, IHechosProxyRepository hechosProxyRepository) {
        this.repoColeccion = repoColeccion;
        this.repoProvincia = repoProvincia;
        this.repoCategoria = repoCategoria;
        this.repoSoliElimHecho = repoSoliElimHecho;
        this.hechosDinamicaRepository = hechosDinamicaRepository;
        this.hechosEstaticaRepository = hechosEstaticaRepository;
        this.hechosProxyRepository = hechosProxyRepository;
        this.ubicacionRepository = ubicacionRepository;
    }

    @Override
    @Transactional
    public List<ColeccionProvincia> obtenerMayorCantHechosProvinciaEnColeccion() {
        // De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados?

        List<Coleccion> colecciones = repoColeccion.findAllByActivoTrue();

        List<ColeccionProvincia> coleccionProvincias = new ArrayList<>();

        for (Coleccion coleccion : colecciones){
            if (!coleccion.getHechos().isEmpty()){
                List<Long> idsHechosEstatica = coleccion.getHechos().stream().
                        filter(h->h.getKey().getFuente().equals(Fuente.ESTATICA))
                        .map(h->h.getKey().getId()).toList();
                List<Long> idsHechosDinamica = coleccion.getHechos().stream().
                        filter(h->h.getKey().getFuente().equals(Fuente.DINAMICA))
                        .map(h->h.getKey().getId()).toList();
                List<Long> idsHechosProxy = coleccion.getHechos().stream().
                        filter(h->h.getKey().getFuente().equals(Fuente.PROXY))
                        .map(h->h.getKey().getId()).toList();

                List<ProvinciaCantidad> cantidadesXProvincia = new ArrayList<>();

                for (Long idHechoEstatica: idsHechosEstatica){
                    Long ubicacion_id = hechosEstaticaRepository.findUbicacionIdByHechoId(idHechoEstatica);

                    Ubicacion ubicacion = ubicacionRepository.findById(ubicacion_id).orElse(null);
                    if (ubicacion != null){
                        Provincia provincia = ubicacion.getProvincia();
                        if (provincia != null){
                            Long provincia_id = provincia.getId();
                           ProvinciaCantidad cantidadProvincia = cantidadesXProvincia.stream()
                                    .filter(cp -> cp.getProvincia_id().equals(provincia_id))
                                    .findAny()
                                   .orElse(null);

                            if (cantidadProvincia == null){
                                cantidadesXProvincia.add(new ProvinciaCantidad(provincia_id));
                            }
                            else{
                                cantidadProvincia.incrementarCantidad();
                            }

                        }
                    }

                }

                for (Long idHechoDinamica: idsHechosDinamica){
                    Long ubicacion_id = hechosDinamicaRepository.findUbicacionIdByHechoId(idHechoDinamica);

                    Ubicacion ubicacion = ubicacionRepository.findById(ubicacion_id).orElse(null);
                    if (ubicacion != null){
                        Provincia provincia = ubicacion.getProvincia();
                        if (provincia != null){
                            Long provincia_id = provincia.getId();
                            ProvinciaCantidad cantidadProvincia = cantidadesXProvincia.stream()
                                    .filter(cp -> cp.getProvincia_id().equals(provincia_id))
                                    .findAny()
                                    .orElse(null);

                            if (cantidadProvincia == null){
                                cantidadesXProvincia.add(new ProvinciaCantidad(provincia_id));
                            }
                            else{
                                cantidadProvincia.incrementarCantidad();
                            }

                        }
                    }

                }

                for (Long idHechoProxy: idsHechosProxy){
                    Long ubicacion_id = hechosProxyRepository.findUbicacionIdByHechoId(idHechoProxy);

                    Ubicacion ubicacion = ubicacionRepository.findById(ubicacion_id).orElse(null);
                    if (ubicacion != null){
                        Provincia provincia = ubicacion.getProvincia();
                        if (provincia != null){
                            Long provincia_id = provincia.getId();
                            ProvinciaCantidad cantidadProvincia = cantidadesXProvincia.stream()
                                    .filter(cp -> cp.getProvincia_id().equals(provincia_id))
                                    .findFirst()
                                    .orElse(null);

                            if (cantidadProvincia == null){
                                cantidadesXProvincia.add(new ProvinciaCantidad(provincia_id));
                            }
                            else{
                                cantidadProvincia.incrementarCantidad();
                            }

                        }
                    }

                }
                cantidadesXProvincia.sort(Comparator.comparing(ProvinciaCantidad::getCantidad).reversed());
                ProvinciaCantidad resultadoFinalColeccion = cantidadesXProvincia.get(0);
                coleccionProvincias.add(new ColeccionProvincia(coleccion.getId(), resultadoFinalColeccion.getProvincia_id(), resultadoFinalColeccion.getCantidad()));
            }
        }

        if (coleccionProvincias.isEmpty())
            return null;

        return coleccionProvincias;
    }


    @Override
    public CategoriaCantidad categoriaMayorCantHechos() {
        List<CategoriaCantidadProjection> infoEstatica = hechosEstaticaRepository.categoriaMayorCantHechos().orElse(null);
        List<CategoriaCantidadProjection> infoDinamica = hechosDinamicaRepository.categoriaMayorCantHechos().orElse(null);
        List<CategoriaCantidadProjection> infoProxy = hechosProxyRepository.categoriaMayorCantHechos().orElse(null);

        List<CategoriaCantidadProjection> infoOrdenada = new ArrayList<>();

        if (infoEstatica != null)
            infoOrdenada.addAll(infoEstatica);
        if (infoDinamica != null)
            infoOrdenada.addAll(infoDinamica);
        if (infoProxy != null)
            infoOrdenada.addAll(infoProxy);

        if (!infoOrdenada.isEmpty()){
            infoOrdenada.sort(Comparator.comparing(CategoriaCantidadProjection::getCantHechos).reversed());
            CategoriaCantidadProjection infoFinal = infoOrdenada.get(0);
            Long categoria_id = infoFinal.getCategoriaId() != null ? infoFinal.getCategoriaId() : null;
            return new CategoriaCantidad(categoria_id, infoFinal.getCantHechos());
        }
        return null;
    }


    @Override
    public List<CategoriaProvincia> mayorCantHechosCategoriaXProvincia() {
        // De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados?

        // ¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?

        // Primero, se parte de las categorias
        List<Categoria> categorias = repoCategoria.findAll();

        List<CategoriaProvincia> categoriaProvincias = new ArrayList<>();

        for (Categoria categoria : categorias){
            if (!categorias.isEmpty()){

                List<HechoEstatica> hechosEstatica = hechosEstaticaRepository.findAllByCategoriaId(categoria.getId());
                List<HechoDinamica> hechosDinamica = hechosDinamicaRepository.findAllByCategoriaId(categoria.getId());
                List<HechoProxy> hechosProxy = hechosProxyRepository.findAllByCategoriaId(categoria.getId());

                List<ProvinciaCantidad> cantidadesXProvincia = new ArrayList<>();

                for (HechoEstatica hechoEstatica: hechosEstatica){
                    Long ubicacion_id = hechoEstatica.getAtributosHecho().getUbicacion_id();
                    if (ubicacion_id!=null){
                        Ubicacion ubicacion = ubicacionRepository.findById(ubicacion_id).orElse(null);

                        if (ubicacion != null){
                            Provincia provincia = ubicacion.getProvincia();
                            if (provincia != null){
                                Long provincia_id = provincia.getId();
                                ProvinciaCantidad cantidadProvincia = cantidadesXProvincia.stream()
                                        .filter(cp -> cp.getProvincia_id().equals(provincia_id))
                                        .findFirst()
                                        .orElse(null);

                                if (cantidadProvincia == null){
                                    cantidadesXProvincia.add(new ProvinciaCantidad(provincia_id));
                                }
                                else{
                                    cantidadProvincia.incrementarCantidad();
                                }

                            }
                        }
                    }
                }

                for (HechoDinamica hechoDinamica: hechosDinamica){
                    Long ubicacion_id = hechoDinamica.getAtributosHecho().getUbicacion_id();
                    if (ubicacion_id!=null){
                        Ubicacion ubicacion = ubicacionRepository.findById(ubicacion_id).orElse(null);

                        if (ubicacion != null){
                            Provincia provincia = ubicacion.getProvincia();
                            if (provincia != null){
                                Long provincia_id = provincia.getId();
                                ProvinciaCantidad cantidadProvincia = cantidadesXProvincia.stream()
                                        .filter(cp -> cp.getProvincia_id().equals(provincia_id))
                                        .findAny()
                                        .orElse(null);

                                if (cantidadProvincia == null){
                                    cantidadesXProvincia.add(new ProvinciaCantidad(provincia_id));
                                }
                                else{
                                    cantidadProvincia.incrementarCantidad();
                                }

                            }
                        }
                    }
                }

                for (HechoProxy hechoProxy: hechosProxy){
                    Long ubicacion_id = hechoProxy.getAtributosHecho().getUbicacion_id();
                    if (ubicacion_id!=null){
                        Ubicacion ubicacion = ubicacionRepository.findById(ubicacion_id).orElse(null);

                        if (ubicacion != null){
                            Provincia provincia = ubicacion.getProvincia();
                            if (provincia != null){
                                Long provincia_id = provincia.getId();
                                ProvinciaCantidad cantidadProvincia = cantidadesXProvincia.stream()
                                        .filter(cp -> cp.getProvincia_id().equals(provincia_id))
                                        .findAny()
                                        .orElse(null);

                                if (cantidadProvincia == null){
                                    cantidadesXProvincia.add(new ProvinciaCantidad(provincia_id));
                                }
                                else{
                                    cantidadProvincia.incrementarCantidad();
                                }

                            }
                        }
                    }
                }
                cantidadesXProvincia.sort(Comparator.comparing(ProvinciaCantidad::getCantidad).reversed());
                ProvinciaCantidad resultadoFinalColeccion = cantidadesXProvincia.get(0);
                categoriaProvincias.add(new CategoriaProvincia(categoria.getId(), resultadoFinalColeccion.getProvincia_id(), resultadoFinalColeccion.getCantidad()));
            }
        }

        if (categoriaProvincias.isEmpty())
            return null;

        return categoriaProvincias;
    }



    @Override
    public List<CategoriaHora> horaMayorCantHechos() {

        List<HoraCategoriaProjection> infoEstatica = hechosEstaticaRepository.horaMayorCantHechos().orElse(null);
        List<HoraCategoriaProjection> infoDinamica = hechosDinamicaRepository.horaMayorCantHechos().orElse(null);
        List<HoraCategoriaProjection> infoProxy = hechosProxyRepository.horaMayorCantHechos().orElse(null);

        List<HoraCategoriaProjection> infoOrdenada = new ArrayList<>();

        if (infoEstatica != null)
            infoOrdenada.addAll(infoEstatica);
        if (infoDinamica != null)
            infoOrdenada.addAll(infoDinamica);
        if (infoProxy != null)
            infoOrdenada.addAll(infoProxy);

        if (!infoOrdenada.isEmpty()){
            List<CategoriaHora> infos = new ArrayList<>();

            for(HoraCategoriaProjection cpp : infoOrdenada){
                Categoria categoria = cpp.getIdCategoria() != null ? repoCategoria.findById(cpp.getIdCategoria()).orElse(null) : null;
                Long categoria_id = categoria!=null? categoria.getId() : null;
                CategoriaHora categoriaHora = new CategoriaHora(categoria_id,cpp.getHoraDelDia(),cpp.getTotalHechos());
                infos.add(categoriaHora);
            }
            return infos;
        }

        return null;
    }

    @Override
    public Long cantSolicitudesEliminacionSpam() {
        return repoSoliElimHecho.obtenerCantSolicitudesEliminacionSpam();
    }

    @Override
    public Categoria findCategoriaById(Long id){
        if (id == null)
            return null;
        return this.repoCategoria.findById(id).orElse(null);
    }

    @Override
    public Provincia findProvinciaById(Long id){
        if (id == null)
            return null;
        return this.repoProvincia.findById(id).orElse(null);
    }

    @Override
    public Coleccion findColeccionById(Long id){
        if (id == null)
            return null;
        return this.repoColeccion.findById(id).orElse(null);
    }
}

/*
-- De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados?

SELECT
p.id         AS provincia_id,
p.nombre     AS provincia,
COUNT(*)     AS total_hechos
FROM coleccion c
JOIN coleccion_hecho ch ON ch.coleccion_id = c.id
JOIN hecho h            ON h.id = ch.hecho_id
JOIN ubicacion u        ON u.id = h.ubicacion_id
JOIN provincia p        ON p.id = h.provincia_id
WHERE c.id = ?                      -- ← id de la colección
GROUP BY p.id, p.nombre
ORDER BY total_hechos DESC;

        -- ¿Cuál es la categoría con mayor cantidad de hechos reportados?

SELECT *
FROM coleccion as c
JOIN coleccion_hecho as ch ON ch.coleccion_id = c.id
JOIN hecho as h ON c.id = ch.hecho_id
ORDER BY COUNT(h.id) DESC
LIMIT 1;

        -- ¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?

SELECT p.id, p.nombre, COUNT(h.id) AS cant_hechos
FROM provincia as p
JOIN ubicacion AS u ON u.provincia_id = p.id
JOIN hecho AS h ON h.ubicacion_id = u.id
WHERE h.categoria_id = categoriaId -- variable que se trae de java
GROUP BY p.id, p.nombre
ORDER BY cant_hechos DESC
LIMIT 1;

        -- ¿A qué hora del día ocurren la mayor cantidad de hechos de una cierta categoría?

SELECT
HOUR(h.fecha_hora) AS hora_del_dia,
COUNT(*)           AS total
FROM hecho h
WHERE h.categoria_id = categoriaId -- variable que se trae de java
GROUP BY HOUR(h.fecha_hora)
ORDER BY total DESC
LIMIT 1;

        -- ¿Cuántas solicitudes de eliminación son spam?

SELECT COUNT (s.id) as total_spam
FROM solicitud_hecho AS s
WHERE s.rechazada_por_spam = 1 AND s.tipoSolicitud = 'SOLICITUD_ELIMINAR';
*/