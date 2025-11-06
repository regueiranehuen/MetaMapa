package modulos.Front.providers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import modulos.Front.dtos.input.AuthResponseDTO;
import modulos.Front.dtos.input.LoginDtoInput;
import modulos.Front.services.WebApiCallerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

// Spring security se encarga de llamar a este metodo
@Component
@AllArgsConstructor
public class CustomAuthProvider implements AuthenticationProvider {

    private final WebApiCallerService webApiCallerService;

   /*
    <form th:action="@{/login}" method="post">
  <input type="text"     name="username" placeholder="Usuario">
  <input type="password" name="password" placeholder="Contraseña">

  <!-- CSRF -->
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
  <button type="submit">Ingresar</button>
</form>

*/
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {


        String username = authentication.getName(); // Llega del form de login
        String password = authentication.getCredentials().toString();

        System.out.println("USUARIO DE RE MIL x: "+username);

        System.out.println("PASSWORD DE RE MIL x: "+ password);

        try{
            LoginDtoInput dtoInput = LoginDtoInput.builder()
                    .nombreDeUsuario(username)
                    .contrasenia(password)
                    .build();
            ResponseEntity<?> rta = webApiCallerService.login(dtoInput, AuthResponseDTO.class);

            if (!rta.getStatusCode().is2xxSuccessful()){
                System.out.println("Usuario o contraseña inválidos");
                throw new BadCredentialsException("Usuario o contraseña inválidos");
            }

            AuthResponseDTO authResponse = (AuthResponseDTO) rta.getBody();

            // Acceder a atributos actuales de la request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // Con el objeto request, se puede setear atributos en la sesión del ususario (lado del servidor)
            // El objeto request provee forma de poder acceder al espacio de sesión para ese usuario

            request.getSession().setAttribute("accessToken", authResponse.getAccessToken());
            request.getSession().setAttribute("refreshToken", authResponse.getRefreshToken());
            request.getSession().setAttribute("username", username);

            List<GrantedAuthority> authorities = new ArrayList<>();

            authorities.add(new SimpleGrantedAuthority("ROLE_" + authResponse.getRol().name()));

            return new UsernamePasswordAuthenticationToken(username, password, authorities);

        } catch (RuntimeException e){
            System.out.println("Error en el sistema de autenticación: " + e.getMessage());
            throw new BadCredentialsException("Error en el sistema de autenticación: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
