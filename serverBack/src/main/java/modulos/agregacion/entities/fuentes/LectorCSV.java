package modulos.agregacion.entities.fuentes;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import modulos.agregacion.entities.DbEstatica.Dataset;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
import modulos.agregacion.entities.atributosHecho.Origen;
import modulos.buscadores.*;
import modulos.shared.utils.FechaParser;
import modulos.shared.utils.Geocodificador;
import modulos.shared.utils.GestorArchivos;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class LectorCSV {
    private static List<String> campos = new ArrayList<>(Arrays.asList("titulo","descripcion","categoria","latitud","longitud","fechadelhecho","pais","provincia"));
    private Dataset dataSet;

    public LectorCSV(Dataset dataSet){
        this.dataSet=dataSet;
    }

    // Entrega 3: los hechos no se pisan los atributos

    public List<HechoEstatica> leerCSV(Usuario usuario, BuscadoresRegistry buscadores) {

        List<HechoEstatica> hechosASubir = new ArrayList<>();

        try {
            Reader reader = new InputStreamReader(new FileInputStream(this.dataSet.getStoragePath()), StandardCharsets.ISO_8859_1);
            //Reader reader = new InputStreamReader(new FileInputStream(this.dataSet), Charset.forName("ISO-8859-1"));
            CSVFormat formato = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withDelimiter(';')
                    .withQuote('"'); // Esto es por defecto, pero podés dejarlo explícito
            CSVParser parser = new CSVParser(reader, formato);

            List<String> headers = parser.getHeaderNames();


            // Se cambia el delimitador a una ','
            if(headers.size() == 1){
                parser.close();

                reader = new FileReader(this.dataSet.getStoragePath());

                formato = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withDelimiter(',')
                        .withQuote('"'); // Esto es por defecto, pero podés dejarlo explícito
                parser = new CSVParser(reader, formato);

                headers = parser.getHeaderNames();


            }

            System.out.println(headers);
            List<Integer> indicesColumnas = this.filtrarColumnas(headers);
            System.out.println("COLUMNAS: " + indicesColumnas);
            //indicesColumnas.forEach(i->i.);

            List<CSVRecord> registrosCSV = parser.getRecords();

            for (int f = 0; f < registrosCSV.size(); f++) {
                System.out.println("ESTOY EN FILA " + f);
                CSVRecord fila = registrosCSV.get(f);

                List<String> registros = new ArrayList<>();
                fila.forEach(registros::add);

                HechoEstatica hecho = new HechoEstatica();

                hecho.getAtributosHecho().setOrigen(Origen.FUENTE_ESTATICA);

                boolean tituloRepetido = false;

                hecho.getAtributosHecho().setTitulo((indicesColumnas.get(0) != -1) ? registros.get(indicesColumnas.get(0)) : null);
                System.out.println("TITULO: " + hecho.getAtributosHecho().getTitulo());
                //Se leen los de fuente estatica
                if(hecho.getAtributosHecho().getTitulo() != null) {
                    Integer cantidadTitulosIguales = buscadores.getBuscadorHecho().buscarCantTituloIgual(hecho.getAtributosHecho().getTitulo());
                    for (HechoEstatica hecho1 : hechosASubir) {
                        if (hecho1.getAtributosHecho().getTitulo() != null && hecho1.getAtributosHecho().getTitulo().equals(hecho.getAtributosHecho().getTitulo())) {
                            cantidadTitulosIguales += 1;
                            break;
                        }
                    }
                    if (cantidadTitulosIguales != 0) {
                        tituloRepetido = true;
                    }
                }
                hecho.getAtributosHecho().setDescripcion((indicesColumnas.get(1) != -1) ? registros.get(indicesColumnas.get(1)) : null);
                System.out.println("DESCRIPCION: " + hecho.getAtributosHecho().getDescripcion());

                String categoriaString = indicesColumnas.get(2) != -1 ? registros.get(indicesColumnas.get(2)) : null;
                Categoria categoria = buscadores.getBuscadorCategoria().buscar(categoriaString);
                hecho.getAtributosHecho().setCategoria_id(categoria != null ? categoria.getId() : null);
                System.out.println("CATEGORIA: " + hecho.getAtributosHecho().getCategoria_id());

                UbicacionString ubicacionString = null;
                Pais pais = null;
                Provincia provincia = null;
                Ubicacion ubicacion = null;

                if (indicesColumnas.get(3) != -1 && indicesColumnas.get(4) != -1 &&
                        (!registros.get(indicesColumnas.get(3)).isEmpty() && !registros.get(indicesColumnas.get(4)).isEmpty())) {
                    Double latitud = Double.parseDouble(registros.get(indicesColumnas.get(3)));
                    Double longitud = Double.parseDouble(registros.get(indicesColumnas.get(4)));
                    ubicacionString = Geocodificador.obtenerUbicacion(latitud, longitud);
                    if(ubicacionString == null) {
                        System.out.println("El geocodificador esta todo cogido");
                    }
                    hecho.getAtributosHecho().setLatitud(latitud);
                    hecho.getAtributosHecho().setLongitud(longitud);
                    System.out.println("LATITUD: " + hecho.getAtributosHecho().getLatitud());
                    System.out.println("LONGITUD: " + hecho.getAtributosHecho().getLongitud());

                    // TODO
                }
                else {
                    ubicacionString = new UbicacionString();
                    ubicacionString.setPais(indicesColumnas.get(6) != -1 ? registros.get(indicesColumnas.get(6)) : null);
                    ubicacionString.setProvincia(indicesColumnas.get(7) != -1 ? registros.get(indicesColumnas.get(7)) : null);

                    System.out.println("PAIS: " + ubicacionString.getPais());
                    System.out.println("PROVINCIA: " + ubicacionString.getProvincia());
                }

                if (ubicacionString != null){
                    pais = buscadores.getBuscadorPais().buscar(ubicacionString.getPais());
                    if (pais!=null){
                        provincia = buscadores.getBuscadorProvincia().buscarConPais(ubicacionString.getProvincia(), pais.getId());
                    }

                    ubicacion = buscadores.getBuscadorUbicacion().buscarOCrear(pais, provincia);
                    if (ubicacion != null)
                        hecho.getAtributosHecho().setUbicacion_id(ubicacion.getId());

                }else{
                    hecho.getAtributosHecho().setUbicacion_id(null);
                }

                System.out.println("UBICACION: " + hecho.getAtributosHecho().getUbicacion_id());

                //System.out.println("Soy una fecha asquerosa: " + FechaParser.parsearFecha(registros.get(indicesColumnas.get(5))));

                hecho.getAtributosHecho().setFechaAcontecimiento((indicesColumnas.get(5) != -1) ? FechaParser.parsearFecha(registros.get(indicesColumnas.get(5))) : null);
                System.out.println("FECHA ACONTECIMIENTO: " + hecho.getAtributosHecho().getFechaAcontecimiento());

                hecho.getAtributosHecho().setModificado(true);
                hecho.setUsuario_id(usuario.getId());
                hecho.getAtributosHecho().setFuente(Fuente.ESTATICA);
                hecho.getDatasets().add(this.dataSet);
                if (tituloRepetido){
                    List<HechoEstatica> hechosIdenticos = buscadores.getBuscadorHecho().existenHechosIdenticos(hecho, hechosASubir);
                    System.out.println("HOLAAAA SOY UN HECHO REPETIDO");
                    if (!hechosIdenticos.isEmpty()) {
                        for (HechoEstatica hechoIdentico : hechosIdenticos) {
                            hechoIdentico.getAtributosHecho().setModificado(true);
                            if (hechoIdentico.getDatasets().stream().filter(d -> d.getFuente().equals(this.dataSet.getFuente()))
                                    .findFirst()
                                    .isEmpty()) {
                                System.out.println("MISMA FUENTE");
                                hechoIdentico.getDatasets().add(this.dataSet);
                                hechosASubir.add(hechoIdentico); // LO AÑADO PARA QUE SE UPDATEE EN importarHechos
                            }
                        }
                        continue;
                    }
                }
                System.out.println("HOLA VOY A SUBIR UN HECHO DIVERTIDO!");
                hechosASubir.add(hecho);
            }
            System.out.println("SIZE DE LA LISTA: " + hechosASubir.size());
            parser.close();
            GestorArchivos.eliminarArchivo(this.dataSet.getStoragePath());
        }
        catch(IOException e){
            throw new RuntimeException("Error al leer el archivo CSV: " + e.getMessage(), e);
        }

        return hechosASubir;
    }

    /**
     * Variante con header opcional.
     * - Si 'header' != null y no está vacío, se escribe como primera fila.
     * - Luego se parte valoresLineales en filas de 'columnasPorFila' (con padding si falta).


     Titulo: Corte parcial de Av. 9 de Julio
     Descripcion: Reducción de carriles por obras
     Categoría:
     Provincia: Ciudad Autónoma de Buenos Aires
     Pais: Argentina
     Fecha del hecho: null
     */
    public static Path generarCsvDesdeListaLineal(
            List<?> valoresLineales,
            String nombreArchivo,
            List<String> header
    ) {
        int columnasPorFila = header.size();
        if (columnasPorFila <= 0) {
            throw new IllegalArgumentException("columnasPorFila debe ser >= 1");
        }
        if (valoresLineales == null) {
            throw new IllegalArgumentException("valoresLineales no puede ser null");
        }

        Path path = Path.of(nombreArchivo);
        try (FileWriter writer = new FileWriter(path.toFile())) {

            // Header opcional
            if (header != null && !header.isEmpty()) {
                // Si el header tiene menos/más columnas, lo ajustamos (padding/truncate)
                List<String> h = new ArrayList<>(columnasPorFila);
                for (int i = 0; i < columnasPorFila; i++) {
                    String col = (i < header.size()) ? header.get(i) : "col" + (i + 1);
                    h.add(escape(col));
                }
                writer.write(String.join(",", h) + "\n");
            }

            // Partimos la lista en bloques de 'columnasPorFila'
            for (int i = 0; i < valoresLineales.size(); i += columnasPorFila) {
                List<String> fila = new ArrayList<>(columnasPorFila);
                for (int j = 0; j < columnasPorFila; j++) {
                    int idx = i + j;
                    Object v = (idx < valoresLineales.size()) ? valoresLineales.get(idx) : ""; // padding
                    fila.add(escape(csvString(v)));
                }
                writer.write(String.join(",", fila) + "\n");
            }

            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }



    public static Path generarCsvDesdeObjeto(Object obj, String nombreArchivo) {
        if (obj == null) throw new IllegalArgumentException("obj no puede ser null");

        Path path = Path.of(nombreArchivo);
        try (var writer = new FileWriter(path.toFile())) {
            escribirComoCsv(obj, writer);
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (IntrospectionException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /* ===== Helpers ===== */

    private static void escribirComoCsv(Object obj, Writer writer)
            throws IOException, IntrospectionException, ReflectiveOperationException {

        if (esSimple(obj)) {
            // Objeto simple: una sola línea sin encabezado
            writer.write(obj.toString() + "\n");
            return;
        }

        if (obj instanceof Collection<?> col) {
            escribirColeccion(col, writer);
            return;
        }

        // POJO: usamos getters
        escribirPojo(obj, writer);
    }

    private static void escribirColeccion(Collection<?> col, Writer writer)
            throws IOException, IntrospectionException, ReflectiveOperationException {

        if (col.isEmpty()) return;

        Object first = col.iterator().next();
        if (esSimple(first)) {
            for (Object o : col) {
                writer.write(o.toString() + "\n");
            }
            return;
        }

        // Colección de POJOs
        var props = propsDe(first.getClass());
        escribirHeader(props, writer);
        for (Object o : col) {
            escribirFilaPojo(o, props, writer);
        }
    }

    private static void escribirPojo(Object pojo, Writer writer)
            throws IOException, IntrospectionException, ReflectiveOperationException {

        var props = propsDe(pojo.getClass());
        escribirHeader(props, writer);
        escribirFilaPojo(pojo, props, writer);
    }

    private static List<PropertyDescriptor> propsDe(Class<?> clazz) throws IntrospectionException {
        var info = Introspector.getBeanInfo(clazz, Object.class);
        return Arrays.stream(info.getPropertyDescriptors())
                .filter(pd -> pd.getReadMethod() != null)
                .sorted(Comparator.comparing(PropertyDescriptor::getName))
                .toList();
    }

    private static void escribirHeader(List<PropertyDescriptor> props, Writer writer) throws IOException {
        writer.write(props.stream()
                .map(PropertyDescriptor::getName)
                .collect(Collectors.joining(",")) + "\n");
    }

    private static void escribirFilaPojo(Object pojo, List<PropertyDescriptor> props, Writer writer)
            throws IOException, ReflectiveOperationException {

        var valores = new ArrayList<String>();
        for (var pd : props) {
            Object v = pd.getReadMethod().invoke(pojo);
            valores.add(v == null ? "" : v.toString());
        }
        writer.write(String.join(",", valores) + "\n");
    }

    private static boolean esSimple(Object o) {
        if (o == null) return true;
        Class<?> c = o.getClass();
        return c.isPrimitive()
                || Number.class.isAssignableFrom(c)
                || CharSequence.class.isAssignableFrom(c)
                || Boolean.class.isAssignableFrom(c)
                || Enum.class.isAssignableFrom(c);
    }

    private static String csvString(Object o) {
        return (o == null) ? "" : o.toString();
    }

    // Escapa comas, comillas y saltos de línea (estilo RFC 4180 básico)
    private static String escape(String s) {
        if (s == null) return "";
        boolean necesita = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!necesita) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }


    // Analizo qué columnas me interesan. Solo leo esas después.
    private List<Integer> filtrarColumnas(List<String> headers) {


        List<Integer> indicesColumnas = new ArrayList<>(Collections.nCopies(LectorCSV.campos.size(), -1));


        // Mapa de sinónimos: cada clave es un patrón, el valor es el campo real que representa
        /*Map<String, String> sinonimos = Map.of(
                "titulo_hecho", "titulo",
                "id_hecho", "titulo",
                "evento", "titulo",
                "fecha", "fechadelhecho"
        );*/

        for (int i = 0; i < headers.size(); i++) {
            String valorColumna = headers.get(i);
            valorColumna = Normalizador.normalizar(valorColumna);
            System.out.println("VALOR COLUMNA: " + valorColumna);
            int j = 0;
            for (String campoEsperado : LectorCSV.campos) {
                System.out.println("CAMPO ESPERADO: " + campoEsperado);
                if (valorColumna.equals(campoEsperado)) {
                    indicesColumnas.set(j, i); // i: posicion del campo del header. j: posicion de la lista
                    break;
                }
                j++;
            }

        }
        return indicesColumnas;
    }



}
