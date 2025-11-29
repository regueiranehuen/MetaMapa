package modulos.buscadores;

import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import modulos.agregacion.entities.atributosHecho.AtributosHecho;
import modulos.agregacion.repositories.DbDinamica.IHechosDinamicaRepository;
import modulos.agregacion.repositories.DbEstatica.IHechosEstaticaRepository;
import modulos.agregacion.repositories.DbMain.*;
import modulos.agregacion.repositories.DbProxy.IHechosProxyRepository;
import modulos.shared.utils.FechaParser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class BuscadorHecho {
    private final IHechosEstaticaRepository hechoRepoEstatica;
    private final IHechosDinamicaRepository hechoRepoDinamica;
    private final IHechosProxyRepository hechoRepoProxy;
    private final ICategoriaRepository categoriaRepository;
    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final IUbicacionRepository ubicacionRepository;


    public BuscadorHecho(IHechosEstaticaRepository hechoRepoEstatica, IHechosDinamicaRepository hechoRepoDinamica, IUbicacionRepository ubicacionRepository, IHechosProxyRepository hechoRepoProxy, ICategoriaRepository categoriaRepository, IPaisRepository paisRepository, IProvinciaRepository provinciaRepository) {
        this.hechoRepoEstatica = hechoRepoEstatica;
        this.hechoRepoDinamica = hechoRepoDinamica;
        this.hechoRepoProxy = hechoRepoProxy;
        this.categoriaRepository = categoriaRepository;
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
        this.ubicacionRepository = ubicacionRepository;
    }

    public Hecho buscarOCrearEstatica(String elemento){
        HechoEstatica hecho = this.buscarEstatica(elemento);
        if(hecho == null){
            hecho = new HechoEstatica();
            hecho.getAtributosHecho().setTitulo(elemento);
        }
        return hecho;
    }
    public Hecho buscarOCrearDinamica(String elemento){
        HechoDinamica hecho = this.buscarDinamica(elemento);
        if(hecho == null){
            hecho = new HechoDinamica();
            hecho.getAtributosHecho().setTitulo(elemento);
        }
        return hecho;
    }
    public Hecho buscarOCrearProxy(String elemento){
        HechoProxy hecho = this.buscarProxy(elemento);
        if(hecho == null){
            hecho = new HechoProxy();
            hecho.getAtributosHecho().setTitulo(elemento);
        }
        return hecho;
    }

    public HechoEstatica buscarEstatica(String elemento) {
        return this.hechoRepoEstatica.findByNombreNormalizado(elemento).orElse(null);
    }

    public Integer buscarCantTituloIgual(String elemento) {
        return this.hechoRepoEstatica.findCantByNombreNormalizado(elemento);
    }

    public List<HechoEstatica> buscarListaEstatica(String elemento) {
        return this.hechoRepoEstatica.findAllByNombreNormalizado(elemento);
    }

    public HechoDinamica buscarDinamica(String elemento) {
        return this.hechoRepoDinamica.findByNombreNormalizado(elemento).orElse(null);
    }

    public List<HechoDinamica> buscarListaDinamica(String elemento) {
        return this.hechoRepoDinamica.findAllByNombreNormalizado(elemento);
    }

    public HechoProxy buscarProxy(String elemento) {
        return this.hechoRepoProxy.findByNombreNormalizado(elemento).orElse(null);
    }

    public List<HechoProxy> buscarListaProxy(String elemento) {
        return this.hechoRepoProxy.findAllByNombreNormalizado(elemento);
    }

    public Long findCantDatasetsHecho(Long hecho_id){
        return this.hechoRepoEstatica.findCantDatasetsHecho(hecho_id);
    }

    public Long findCantHechosIgualTituloDiferentesAtributos(Long hecho_id){
        return this.hechoRepoEstatica.findCantHechosIgualTituloDiferentesAtributos(hecho_id);
    }

    private static boolean normEq(String a, String b) {
        // Evita NPE dentro del normalizador
        String s1 = (a == null) ? "" : a;
        String s2 = (b == null) ? "" : b;
        return Normalizador.normalizarYComparar(s1, s2);
    }

    // --- helpers null-safe ---
    private static boolean eqDouble(Double a, Double b, double eps) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        // también evita NaN/Infinity si querés:
        if (!Double.isFinite(a) || !Double.isFinite(b)) return false;
        return Math.abs(a - b) <= eps;
    }


    public List<HechoEstatica> existenHechosIdenticos(HechoEstatica hecho, List<HechoEstatica> hechosASubir) {

        // Si bien ahora ya no van a haber hechos identicos repetidos, hago array list de hechos identicos pq ya hay algunos en la BDD

        List<HechoEstatica> hechos = new ArrayList<>();

        if (hecho != null && hecho.getAtributosHecho() != null) {
            for(HechoEstatica h : hechosASubir){
                System.out.println("LATITUD 1 " + hecho.getAtributosHecho().getLatitud());
                System.out.println("LATITUD 2 " + h.getAtributosHecho().getLatitud());

                if(normEq(h.getAtributosHecho().getTitulo(), hecho.getAtributosHecho().getTitulo())
                && normEq(h.getAtributosHecho().getDescripcion(), hecho.getAtributosHecho().getDescripcion())
                && FechaParser.sonMismaFecha(h.getAtributosHecho().getFechaAcontecimiento(), hecho.getAtributosHecho().getFechaAcontecimiento())
                && Objects.equals(h.getAtributosHecho().getUbicacion_id(), hecho.getAtributosHecho().getUbicacion_id()) &&
                        Objects.equals(h.getAtributosHecho().getCategoria_id(), hecho.getAtributosHecho().getCategoria_id()) &&
                        Objects.equals(h.getAtributosHecho().getLatitud(), hecho.getAtributosHecho().getLatitud()) &&
                        Objects.equals(h.getAtributosHecho().getLongitud(), hecho.getAtributosHecho().getLongitud()))
                {
                    hechos.add(h);
                }
            }
        }

        assert hecho != null;
        assert hecho.getAtributosHecho() != null;

        AtributosHecho atributos = hecho.getAtributosHecho();
        /*LocalDateTime fecha = atributos.getFechaAcontecimiento() != null
                ? atributos.getFechaAcontecimiento()
                : LocalDateTime.of(9999, 12, 31, 23, 59);*/

        hechos.addAll(hechoRepoEstatica.findHechosIdenticos(
                atributos.getTitulo(),
                atributos.getCategoria_id(),
                atributos.getDescripcion(),
                atributos.getUbicacion_id(),
                atributos.getOrigen().codigoEnString(),
                atributos.getFuente().codigoEnString(),
                atributos.getFechaAcontecimiento(),
                atributos.getLatitud(),
                atributos.getLongitud()));

        return hechos;

    }

}
