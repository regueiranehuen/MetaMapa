package modulos.agregacion.services;

import modulos.agregacion.entities.DbMain.Categoria;
import modulos.agregacion.entities.DbMain.Pais;
import modulos.agregacion.entities.DbMain.Provincia;
import modulos.agregacion.entities.DbMain.Sinonimo;
import modulos.agregacion.entities.DbMain.usuario.Rol;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
import modulos.agregacion.repositories.DbMain.*;
import modulos.shared.dtos.input.SinonimoInputDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SinonimoService {

    private final ICategoriaRepository categoriaRepository;
    private final ISinonimoRepository sinonimoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;

    public SinonimoService(ICategoriaRepository categoriaRepository, ISinonimoRepository sinonimoRepository, IUsuarioRepository usuarioRepository, IPaisRepository paisRepository, IProvinciaRepository provinciaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.sinonimoRepository = sinonimoRepository;
        this.usuarioRepository = usuarioRepository;
        this.paisRepository = paisRepository;
        this.provinciaRepository = provinciaRepository;
    }

    public ResponseEntity<?> crearSinonimoCategoria(String username, SinonimoInputDto sinonimoDTO) {

        ResponseEntity<?> respuesta = checkeoAdmin(username);

        if (!respuesta.getStatusCode().equals(HttpStatus.OK)){
            return respuesta;
        }

        Categoria categoria = categoriaRepository.findById(sinonimoDTO.getId_entidad()).orElse(null);

        if(categoria == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","El id de la categoria no es valido"));
        }

        Sinonimo sinonimo = sinonimoRepository.findByIdCategoriaAndNombre(sinonimoDTO.getId_entidad(), sinonimoDTO.getSinonimo()).orElse(null);

        if(sinonimo != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El sinonimo ya existe");
        }

        sinonimo = new Sinonimo(sinonimoDTO.getSinonimo());

        categoria.getSinonimos().add(sinonimo);

        categoriaRepository.save(categoria);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<?> crearSinonimoPais(String username, SinonimoInputDto sinonimoDTO) {
        ResponseEntity<?> respuesta = checkeoAdmin(username);

        if (!respuesta.getStatusCode().is2xxSuccessful()){
            return respuesta;
        }

        Pais pais = this.paisRepository.findById(sinonimoDTO.getId_entidad()).orElse(null);

        if(pais == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","El id pais no es valido"));
        }

        Sinonimo sinonimo = this.sinonimoRepository.findByIdPaisAndNombre(sinonimoDTO.getId_pais(), sinonimoDTO.getSinonimo()).orElse(null);

        if(sinonimo != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El sinonimo ya existe");
        }

        sinonimo = new Sinonimo(sinonimoDTO.getSinonimo());

        pais.getSinonimos().add(sinonimo);

        paisRepository.save(pais);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<?> crearSinonimoProvincia(String username, SinonimoInputDto sinonimoDTO) {
        ResponseEntity<?> respuesta = checkeoAdmin(username);

        if (!respuesta.getStatusCode().is2xxSuccessful()){
            return respuesta;
        }

        
        
        

        Pais pais = this.paisRepository.findById(sinonimoDTO.getId_entidad()).orElse(null);

        if(pais == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","El id pais no es valido"));
        }

        Provincia provincia = this.provinciaRepository.findByIdAndPaisId(sinonimoDTO.getId_pais(), sinonimoDTO.getId_entidad()).orElse(null);

        Sinonimo sinonimo = this.sinonimoRepository.findByIdProvinciaAndNombre(sinonimoDTO.getId_pais(), sinonimoDTO.getSinonimo()).orElse(null);

        if(sinonimo != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El sinonimo ya existe");
        }

        sinonimo = new Sinonimo(sinonimoDTO.getSinonimo());

        provincia.getSinonimos().add(sinonimo);

        provinciaRepository.save(provincia);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private ResponseEntity<?> checkeoAdmin(String username){

        if (username == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Usuario usuario = usuarioRepository.findByNombreDeUsuario(username).orElse(null);

        if (usuario == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el usuario");
        }
        else if (!usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuario);
    }


}
