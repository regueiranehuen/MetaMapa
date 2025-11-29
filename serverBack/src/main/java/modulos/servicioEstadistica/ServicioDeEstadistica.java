package modulos.servicioEstadistica;

import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.fuentes.LectorCSV;
import modulos.apis.IDatosQuery;
import modulos.servicioEstadistica.entities.*;
import modulos.servicioEstadistica.repositories.IEstadisticasRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioDeEstadistica {

    private final IDatosQuery datosQuery;
    private final IEstadisticasRepository estadisticasRepository;

    private Estadisticas estadisticasActuales;

    public ServicioDeEstadistica(IDatosQuery datosQuery, IEstadisticasRepository estadisticasRepository) {
        this.datosQuery = datosQuery;
        this.estadisticasRepository = estadisticasRepository;
        // Si querés tener las estadísticas ni bien entrás -> this.generarEstadistica();
        //this.generarEstadistica();
    }

    @Async
    @Scheduled(cron = "${miapp.cron}")//tiempo en properties
    public void generarEstadistica(){

        // Corregido
        // De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados?
        List<ColeccionProvincia> coleccionProvincias = datosQuery.obtenerMayorCantHechosProvinciaEnColeccion();

        // Corregido
        // ¿Cuál es la categoría con mayor cantidad de hechos reportados?
        CategoriaCantidad categoriaCantidad = datosQuery.categoriaMayorCantHechos();

        // Corregido
        // ¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?
        List<CategoriaProvincia> categoriaProvincias = datosQuery.mayorCantHechosCategoriaXProvincia();

        // Corregido
        // ¿A qué hora del día ocurren la mayor cantidad de hechos de una cierta categoría?
        List<CategoriaHora> categoriaHoras = datosQuery.horaMayorCantHechos();

        // Corregido
        // ¿Cuántas solicitudes de eliminación son spam?
        Long cantidadDeSpam = datosQuery.cantSolicitudesEliminacionSpam();
        Estadisticas estadisticas = new Estadisticas(cantidadDeSpam,
            categoriaCantidad, categoriaHoras, categoriaProvincias, coleccionProvincias);



        this.estadisticasActuales = estadisticas;
        this.estadisticasRepository.save(estadisticas);
    }


//id 1 : spam id 2 : categoria con mayor cant de hechos id 3: hora de mayor cantidad de hechos id 4: provincia con mayor cantidad de hechos de una categoria
//id 5 : provincia con mayor cantidad de hechos de una coleccion
    public Path exportarEstadisticaSpam() {
        if (this.estadisticasActuales == null || this.estadisticasActuales.getCantidadDeSpam() == null) {
            return null;
        }

        List<Object> valores;
        List<String> header;
        String nombre = "estadisticaSpam.csv";
        header = List.of("totalSpam");
        Long total = this.estadisticasActuales.getCantidadDeSpam();

        valores = List.of(total);
        return LectorCSV.generarCsvDesdeListaLineal(valores, nombre, header);
    }

    public Path exportarEstadisticaCategoriaMayorCantidadHechos() {
        if (this.estadisticasActuales == null || this.estadisticasActuales.getCategoriaCantidad() == null) {
            return null;
        }
        List<Object> valores = new ArrayList<>();
        List<String> header;
        String nombre = "estadisticaCategoriaMasHechos.csv";
        header = List.of("categoria_id", "categoria_titulo", "cantidad");

            var cc = this.estadisticasActuales.getCategoriaCantidad();
            Long categoria_id = Optional.ofNullable(cc)
                    .map(CategoriaCantidad::getCategoria_id)
                    .orElse(null);
            Categoria categoria = datosQuery.findCategoriaById(categoria_id);
            String categoriaStr = categoria != null ? categoria.getTitulo() : null;

            Integer cantidad = Optional.ofNullable(cc)
                    .map(CategoriaCantidad::getCantidad)
                    .orElse(0);

            valores.add(categoria_id);
            valores.add(categoriaStr);
            valores.add(cantidad);
            return LectorCSV.generarCsvDesdeListaLineal(valores, nombre, header);


    }

    public Path exportarEstadisticaHoraMayorCantidadHechosCategorias(){
        if (this.estadisticasActuales == null || this.estadisticasActuales.getCategoriaHoras() == null) {
            return null;
        }
        List<Object> valores = new ArrayList<>();
        List<String> header;

        String nombre = "estadisticaHoraMasHechosXCategoria.csv";
        header = List.of("categoria_id", "categoria_titulo", "hora", "cantidad");

        List<CategoriaHora> lista = Optional.ofNullable(this.estadisticasActuales.getCategoriaHoras())
                .orElse(List.of());

            for (var v : lista) {
                if (v == null) continue;
                Long categoria_id = v.getCategoria_id();
                Categoria categoria = datosQuery.findCategoriaById(categoria_id);
                String categoriaStr = categoria != null ? categoria.getTitulo() : null;

                Integer hora = v.getHora();
                Integer cant = Optional.ofNullable(v.getCantidad()).orElse(0);

                valores.add(categoria_id);
                valores.add(categoriaStr);
                valores.add(hora);
                valores.add(cant);
            }

            return LectorCSV.generarCsvDesdeListaLineal(valores, nombre, header);
    }


    public Path exportarEstadisticaMayorCantidadHechosCategoriasProvincias(){
        if (this.estadisticasActuales == null || this.estadisticasActuales.getCategoriaProvincias() == null) {
            return null;
        }
        List<Object> valores = new ArrayList<>();
        List<String> header;

        String nombre = "estadisticaProvinciaMasHechosXCategoria.csv";
        header = List.of("categoria_id", "categoria_titulo", "provincia_id", "provincia_nombre", "cantidad");

        List<CategoriaProvincia> lista = Optional.ofNullable(this.estadisticasActuales.getCategoriaProvincias())
                .orElse(List.of());

            for (var v : lista) {
                if (v == null) continue;
                Long categoria_id = v.getCategoria_id();
                Categoria categoria = datosQuery.findCategoriaById(categoria_id);
                String categoriaStr = categoria != null ? categoria.getTitulo() : null;
                Long provincia_id = v.getProvincia_id();
                Provincia provincia = datosQuery.findProvinciaById(provincia_id);
                String provinciaStr = provincia!=null ? provincia.getProvincia() : null;
                Long cant = v.getCantidad();

                valores.add(categoria_id);
                valores.add(categoriaStr);
                valores.add(provincia_id);
                valores.add(provinciaStr);
                valores.add(cant);
            }

            return LectorCSV.generarCsvDesdeListaLineal(valores, nombre, header);
    }

    public Path exportarEstadisticaProvinciaMayorCantidadHechosColecciones(){
        if (this.estadisticasActuales == null || this.estadisticasActuales.getColeccionProvincias() == null) {
            return null;
        }
        List<Object> valores = new ArrayList<>();
        List<String> header;
        String nombre = "estadisticaProvinciaMasHechosXCategoria.csv";
        header = List.of("coleccion_id", "coleccion_titulo", "provincia_id", "provincia_nombre", "cantidad");

        List<ColeccionProvincia> lista = Optional.ofNullable(this.estadisticasActuales.getColeccionProvincias())
                .orElse(List.of());

            for (var v : lista) {
                if (v == null) continue;
                Long coleccion_id = v.getColeccion_id();
                Coleccion coleccion = datosQuery.findColeccionById(coleccion_id);
                String coleccionStr = coleccion!=null?coleccion.getTitulo():null;
                Long provincia_id = v.getProvincia_id();
                Provincia provincia = datosQuery.findProvinciaById(provincia_id);
                String provinciaStr = provincia!=null ? provincia.getProvincia() : null;
                Long cant = v.getCantidad();

                valores.add(coleccion_id);
                valores.add(coleccionStr);
                valores.add(provincia_id);
                valores.add(provinciaStr);
                valores.add(cant);
            }

            return LectorCSV.generarCsvDesdeListaLineal(valores, nombre, header);
    }

    /*switch (id) {





        case 4: { // Provincia con más hechos por categoría
            String nombre = "estadisticaProvinciaMasHechosXCategoria.csv";
            header = List.of("categoria", "cantidad", "provincia");

            List<CategoriaProvincia> lista = Optional.ofNullable(snap.getCategoriaProvincias())
                    .orElse(List.of());

            /*for (var v : lista) {
                if (v == null) continue;
                String categoria = Optional.ofNullable(v.getCategoria())
                        .map(Categoria::getTitulo)
                        .orElse(null);
                String provincia = Optional.ofNullable(v.getProvincia())
                        .map(Provincia::getProvincia)
                        .orElse(null);
                Integer cant = Optional.ofNullable(v.getCantidad()).orElse(0);

                valores.add(categoria);
                valores.add(cant);
                valores.add(provincia);
            }

            return LectorCSV.generarCsvDesdeListaLineal(valores, nombre, header);
        }

        case 5: { // Provincia con más hechos por colección
            String nombre = "estadisticaProvinciaMasHechosXColeccion.csv";
            header = List.of("coleccion", "cantidad", "provincia");

            List<ColeccionProvincia> lista = Optional.ofNullable(snap.getColeccionProvincias())
                    .orElse(List.of());

            /*for (var v : lista) {
                if (v == null) continue;
                String coleccion = Optional.ofNullable(v.getColeccion())
                        .map(Coleccion::getTitulo)
                        .orElse(null);
                String provincia = Optional.ofNullable(v.getProvincia())
                        .map(Provincia::getProvincia)
                        .orElse(null);
                Integer cant = Optional.ofNullable(v.getCantidad()).orElse(0);

                valores.add(coleccion);
                valores.add(provincia);
                valores.add(cant);
            }

            return LectorCSV.generarCsvDesdeListaLineal(valores, nombre, header);
        }

        default:
            return null;
    }*/
}








/*
                Categoria catInc = new Categoria();
                catInc.setTitulo("Incendios");

                Categoria catRobo = new Categoria();
                catRobo.setTitulo("Robos");

                Provincia provBA = new Provincia();
                provBA.setProvincia("Buenos Aires");

                Provincia provCBA = new Provincia();
                provCBA.setProvincia("Córdoba");


                Estadisticas est1 = new Estadisticas();

// Filas (categoria, provincia, cantidad)
                CategoriaProvincia cp1 = new CategoriaProvincia(catInc,  provBA, 120);
                CategoriaProvincia cp2 = new CategoriaProvincia(catInc,  provCBA, 45);
                CategoriaProvincia cp3 = new CategoriaProvincia(catRobo, provCBA, 90);

                est1.getCategoriaProvincias().add(cp1);
                est1.getCategoriaProvincias().add(cp2);
                est1.getCategoriaProvincias().add(cp3);
                // ====== Armado de valores y header para el CSV ======
                List<Object> merca = new ArrayList<>();
                for (CategoriaProvincia valor : est1.getCategoriaProvincias()) {
                    merca.add(valor.getCategoria().getTitulo());     // String
                    merca.add(valor.getProvincia().getProvincia());  // String
                    merca.add(valor.getCantidad());                  // Integer
                }

                Estadisticas falopa = new Estadisticas();
                falopa.setCategoriaCantidad(new CategoriaCantidad());
                falopa.getCategoriaCantidad().setCategoria(new Categoria());
                falopa.getCategoriaCantidad().getCategoria().setTitulo("BOLIVINAS");
                falopa.getCategoriaCantidad().setCantidad(200000000);

                Estadisticas falopa2 = new Estadisticas();
                falopa2.setCategoriaCantidad(new CategoriaCantidad());
                falopa2.getCategoriaCantidad().setCategoria(new Categoria());
                falopa2.getCategoriaCantidad().getCategoria().setTitulo("ARGENTINAS");
                falopa2.getCategoriaCantidad().setCantidad(2);
*/


/*
* SERVICIOA -> tiene inyectado a la interfaz X -> la interfaz X tiene una implementacion con las funcionalidades que necesita A -> se conecta a B
*
* */


/*@GetMapping(value = "/reporte.csv", produces = "text/csv")
public ResponseEntity<String> descargarCsv() {
    StringBuilder csv = new StringBuilder();
    // (opcional) BOM para que Excel lea UTF-8
    csv.append('\uFEFF');
    csv.append("id,nombre\n");
    csv.append("1,Ana\n");
    csv.append("2,Juan\n");

    return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"reporte.csv\"")
            .body(csv.toString());
}
*/