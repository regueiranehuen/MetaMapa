package modulos.Front.services;

import modulos.Front.dtos.input.*;
import modulos.Front.dtos.output.MensajeOutputDTO;
import modulos.Front.dtos.output.UsuarioOutputDto;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



// implements UserDetailsService

@Service
public class UsuarioService {

    private final WebApiCallerService webApiCallerService;
    private String usuarioServiceUrl = "/api/usuario";

    public UsuarioService(WebApiCallerService webApiCallerService) {
        this.webApiCallerService = webApiCallerService;
    }

    /*@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ResponseEntity<?> rta = webApiCallerService.getEntity(usuarioServiceUrl + "/" + username, Usuario.class);

        if (!rta.getStatusCode().is2xxSuccessful()){
            throw new UsernameNotFoundException(username);
        }

        Usuario usuario = (Usuario) rta.getBody();

        List<GrantedAuthority> authorities = new ArrayList<>();

        // El rol no va a venir null. Se chequea en el back antes
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        return User.withUsername(usuario.getNombreDeUsuario())
                .password(usuario.getContrasenia())
                .authorities(authorities)
                .build();

        // En el camino feliz, lo que sigue es abrir la sesión con la cookie (JSESSIONID). Generar el espacio en el server para ese usuario
        // La sesion la hace spring boot
        // Se van a guardar cosas del usuario

        // Spring boot recupera el contexto para el usuario y recien ahi llama al controlador
    }*/

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    public String getUsernameFromSession(){
        return webApiCallerService.getUsernameFromSession();
    }

    public ResponseEntity<?> crearUsuario(UsuarioInputDTO inputDTO){
        return webApiCallerService.postEntityTokenOpcional(this.usuarioServiceUrl + "/public/crear", inputDTO, Void.class);
    }

    public ResponseEntity<?> cambiarContrasenia(CambiarContraseniaDtoInput dto) {
        return webApiCallerService.postEntity(this.usuarioServiceUrl + "/editar/contrasenia", dto, Void.class);
    }

    public ResponseEntity<?> editarUsuario(EditarUsuarioDtoInput dto) {
        return webApiCallerService.postEntity(this.usuarioServiceUrl + "/editar/campos-escalares", dto, Void.class);
    }

    public ResponseEntity<?> editarNombreDeUsuario(EditarNombreDeUsuarioDtoInput dto) {
        return webApiCallerService.postEntity(this.usuarioServiceUrl + "/editar/nombre-usuario", dto, Void.class);
    }

    public ResponseEntity<?> getAll() {
        return webApiCallerService.getEntity(this.usuarioServiceUrl + "/get-all", Void.class);
    }

    public ResponseEntity<UsuarioOutputDto> getUsuario() {
        return webApiCallerService.getEntity(this.usuarioServiceUrl + "/get/usuario", UsuarioOutputDto.class);
    }

    public ResponseEntity<?> obtenerMensajes() {
        return webApiCallerService.getList(this.usuarioServiceUrl + "/get-mensajes", MensajeOutputDTO.class);
    }

}
