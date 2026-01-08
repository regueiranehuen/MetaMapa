package raiz.Front;

@FunctionalInterface
public interface ApiCall<T> {
    T execute(String accessToken) throws Exception; // permite que el lambda lance excepciones
}
