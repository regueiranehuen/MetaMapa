package modulos.Front.services;

import jakarta.servlet.http.HttpServletRequest;
import modulos.Front.ApiCall;
import modulos.Front.dtos.input.AuthResponseDTO;
import modulos.Front.dtos.input.LoginDtoInput;
import modulos.Front.dtos.input.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class WebApiCallerService {

    private final WebClient webClient = WebClient.create("http://localhost:8083");

    private String urlBase = "http://localhost:8080";


    // Method de ezequiel
    public <T> ResponseEntity<T> executeWithTokenRetry(
            java.util.function.Function<String, reactor.core.publisher.Mono<ResponseEntity<T>>> apiCall) {

        String accessToken = getAccessTokenFromSession();
        String refreshToken = getRefreshTokenFromSession();

        TokenResponse tr = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 1) Primer intento con el access token actual
            return apiCall.apply(accessToken).block();

        } catch (WebClientResponseException e) {
            // 2) Si expiró (401/403) y tengo refresh → intento refrescar y reintentar
            if ((e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN)
                    && refreshToken != null) {
                try {
                    AuthResponseDTO newTokens = refreshToken(tr);

                    if (newTokens == null || newTokens.getAccessToken() == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }

                    // Guardar nuevos tokens en sesión
                    ServletRequestAttributes attrs =
                            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                    HttpServletRequest request = attrs.getRequest();
                    request.getSession().setAttribute("accessToken", newTokens.getAccessToken());
                    if (newTokens.getRefreshToken() != null) {
                        request.getSession().setAttribute("refreshToken", newTokens.getRefreshToken());
                    }

                    // 3) Reintento con el nuevo access token
                    try {
                        return apiCall.apply(newTokens.getAccessToken()).block();
                    } catch (WebClientResponseException e2) {
                        return ResponseEntity.status(e2.getStatusCode()).build();
                    } catch (Exception ex2) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                } catch (Exception refreshError) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }

            // 4) Otros códigos HTTP → devuelvo el status correspondiente
            return ResponseEntity.status(e.getStatusCode()).build();

        } catch (Exception e) {
            // 5) Errores no HTTP (timeout, conexión, etc.)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    // Falta lógica de refresh token
    /*public <T> ResponseEntity<T> executeWithTokenRetry(
            java.util.function.Function<String, reactor.core.publisher.Mono<ResponseEntity<T>>> apiCall) {

        String accessToken = getAccessTokenFromSession();
        String refreshToken = getRefreshTokenFromSession();

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return apiCall.apply(accessToken)
                .onErrorResume(WebClientResponseException.class, e -> {
                    // Para cualquier otro error

                    return Mono.just(ResponseEntity.status(e.getStatusCode()).build());
                })
                .onErrorResume(Throwable.class, ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()))
                .block(); // mantiene comportamiento bloqueante final
    }*/





    /**
     * Refresca el access token usando el refresh token
     */
    private AuthResponseDTO refreshToken(TokenResponse tokenResponse) {
        try {


            AuthResponseDTO response = webClient
                    .post()
                    .uri(urlBase + "/auth/refresh")
                    .bodyValue(tokenResponse)
                    .retrieve()
                    .bodyToMono(AuthResponseDTO.class)
                    .block();

            // Actualizar tokens en sesión
            updateTokensInSession(response.getAccessToken(), response.getRefreshToken());
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error al refrescar token: " + e.getMessage(), e);
        }
    }


    /**
     * Actualiza los tokens en la sesión HTTP actual.
     */
    private void updateTokensInSession(String newAccessToken, String newRefreshToken) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        if (newAccessToken != null) {
            request.getSession().setAttribute("accessToken", newAccessToken);
        }

        if (newRefreshToken != null) {
            request.getSession().setAttribute("refreshToken", newRefreshToken);
        }

    }



    /**
     * Obtiene el access token de la sesión
     */
    private String getAccessTokenFromSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        return (String) request.getSession().getAttribute("accessToken");
    }

    /**
     * Obtiene el refresh token de la sesión
     */
    private String getRefreshTokenFromSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        return (String) request.getSession().getAttribute("refreshToken");
    }

    /**
     * Ejecuta una llamada HTTP GET que retorna una lista
     */
    // Method de ezequiel
    /*public <T> List<T> getList(String url, Class<T> responseType) {
        return executeWithTokenRetry(accessToken ->
                webClient
                        .get()
                        .uri(url)
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToFlux(responseType)
                        .collectList()
                        .block()
        );
    }*/

    public <T> ResponseEntity<List<T>> getList(String url, Class<T> elementType) {
        return executeWithTokenRetry(token ->
                webClient.get()
                        .uri(url)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .toEntityList(elementType) // Mono<ResponseEntity<List<T>>>
        );
    }

    public <T> ResponseEntity<T> getEntity(String url, Class<T> elementType){
        return executeWithTokenRetry(token ->
                webClient.get()
                        .uri(url)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .toEntity(elementType)
        );
    }

    public <T> ResponseEntity<T> postEntity(String url, Object body, Class<T> elementType){
        return executeWithTokenRetry(token ->
                webClient.post()
                        .uri(url)
                        .header("Authorization", "Bearer " + token)
                        .bodyValue(body)
                        .retrieve()
                        .toEntity(elementType)
        );
    }

    public <T> ResponseEntity<T> postEntity(String url, Class<T> elementType){
        return executeWithTokenRetry(token ->
                webClient.post()
                        .uri(url)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .toEntity(elementType)
        );
    }



    public <T> ResponseEntity<T> login(Object body, Class<T> elementType) {
        try {
            return webClient
                    .post()
                    .uri(urlBase + "/auth")
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(elementType)
                    .block();


        } catch (WebClientResponseException e) {

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Login fallido - credenciales incorrectas
                return null;
            }
            // Otros errores HTTP
            throw new RuntimeException("Error en el servicio de autenticación: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con el servicio de autenticación: " + e.getMessage(), e);
        }
    }





}