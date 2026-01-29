package com.tpagrupo17.Metamapa.utils;

@FunctionalInterface
public interface ApiCall<T> {
    T execute(String accessToken) throws Exception; // permite que el lambda lance excepciones
}
