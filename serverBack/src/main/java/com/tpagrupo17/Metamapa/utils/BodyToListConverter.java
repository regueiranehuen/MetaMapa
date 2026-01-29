package com.tpagrupo17.Metamapa.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class BodyToListConverter {

    public static <T> List<T> bodyToList(ResponseEntity<?> rta, Class<T> tipo){
        List<?> body = (List<?>) rta.getBody();
        if (body!=null){
            return body.stream()
                    .map(o -> new ObjectMapper().convertValue(o, tipo))
                    .toList();
        }
        return null;
    }
}
