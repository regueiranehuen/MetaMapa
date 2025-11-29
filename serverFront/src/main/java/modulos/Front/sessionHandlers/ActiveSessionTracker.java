package modulos.Front.sessionHandlers;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Component
public class ActiveSessionTracker implements HttpSessionListener {

    private static final Map<String, HttpSession> activeSessions = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ActiveSessionTracker.class);
    public List<HttpSession> sesionesAsociadasAUsuario(String usuario){
        List<HttpSession> sesionesUsuario = new ArrayList<>();
        for (Map.Entry<String, HttpSession> entry : activeSessions.entrySet()) {
            //String key = entry.getKey();          // la clave (por ejemplo el sessionId)
            HttpSession value = entry.getValue(); // el valor (la sesión)
            String usuarioSesion = (String) value.getAttribute("username");
            if (usuarioSesion != null){
                if (usuarioSesion.equals(usuario)){
                    logger.debug("SESION DE ID {} ENCONTRADA PARA EL USUARIO {}", value.getId(), usuarioSesion);
                    sesionesUsuario.add(value);
                }
            }
        }
        return sesionesUsuario;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.put(se.getSession().getId(), se.getSession());
        logger.debug("SESION DE ID {} CREADA", se.getSession().getId());
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions.remove(se.getSession().getId());
    }

    public static HttpSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
}