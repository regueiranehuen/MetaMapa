package modulos.buscadores;

import modulos.agregacion.entities.DbMain.Fuente;
import modulos.agregacion.entities.atributosHecho.Origen;
import modulos.agregacion.entities.atributosHecho.TipoContenido;
import modulos.agregacion.repositories.DbMain.IFiltroRepository;
import org.springframework.stereotype.Component;
import modulos.agregacion.entities.DbMain.filtros.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

@Component
public class BuscadorFiltro {

    private final IFiltroRepository filtrosRepo;

    public BuscadorFiltro(IFiltroRepository filtrosRepo) {
        this.filtrosRepo = filtrosRepo;
    }

    /* ================== Categoria ================== */
    public Optional<FiltroCategoria> buscarFiltroCategoriaPorCategoriaId(Long categoriaId) {
        if (categoriaId == null) return Optional.empty();
        return filtrosRepo.findFiltroCategoriaByCategoriaId(categoriaId);
    }

    /* ========== Contenido Multimedia (Integer/enum) ========== */
    public Optional<FiltroContenidoMultimedia> buscarFiltroContenidoMultimediaPorTipo(TipoContenido tipo) {
        if (tipo == null) return Optional.empty();
        return filtrosRepo.findFiltroContenidoMultimediaByTipo(TipoContenido.fromCodigo(tipo.getCodigo()));
    }

    /* ================== Descripcion (String) ================= */
    public Optional<FiltroDescripcion> buscarFiltroDescripcionExacta(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) return Optional.empty();
        return filtrosRepo.findFiltroDescripcionByDescripcion(descripcion);
    }

    /* ======= Fecha de Acontecimiento (ini / fin) ======= */
    public Optional<FiltroFechaAcontecimiento> buscarFiltroFechaAcontecimientoPorRango(LocalDateTime ini, LocalDateTime fin) {
        if (ini == null && fin == null) return Optional.empty();
        return filtrosRepo.findFiltroFechaAcontecimientoByRango(ini, fin);
    }

    /* ======= Fecha de Carga (ini / fin) ======= */
    public Optional<FiltroFechaCarga> buscarFiltroFechaCargaPorRango(LocalDateTime ini, LocalDateTime fin) {
        if (ini == null && fin == null) return Optional.empty();
        return filtrosRepo.findFiltroFechaCargaByRango(ini, fin);
    }

    /* ================== Origen (Integer/enum) ================= */
    public Optional<FiltroFuente> buscarFiltroFuentePorValor(Integer fuente) {
        if (fuente == null) return Optional.empty();
        return filtrosRepo.findFiltroFuenteByFuente(Fuente.fromCodigo(fuente));
    }

    /* ================== País (entidad) ================= */
    public Optional<FiltroPais> buscarFiltroPaisPorPaisId(Long paisId) {
        if (paisId == null) return Optional.empty();
        return filtrosRepo.findFiltroPaisByPaisId(paisId);
    }

    /* ================== Provincia (entidad) ================= */
    public Optional<FiltroProvincia> buscarFiltroProvinciaPorProvinciaId(Long provinciaId) {
        if (provinciaId == null) return Optional.empty();
        return filtrosRepo.findFiltroProvinciaByProvinciaId(provinciaId);
    }

    /* ================== Título (String) ================= */
    public Optional<FiltroTitulo> buscarFiltroTituloExacto(String titulo) {
        if (titulo == null || titulo.isBlank()) return Optional.empty();
        return filtrosRepo.findFiltroTituloByTitulo(titulo);
    }

}
