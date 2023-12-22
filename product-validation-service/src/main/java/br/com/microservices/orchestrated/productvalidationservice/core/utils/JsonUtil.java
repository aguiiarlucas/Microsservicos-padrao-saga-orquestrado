package br.com.microservices.orchestrated.productvalidationservice.core.utils;

import br.com.microservices.orchestrated.productvalidationservice.core.dto.EventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JsonUtil {


    private final ObjectMapper objectMapper;

    public String toJson(Object obj) {

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }


    }


    public EventDTO toEvent(String json) {
        try {
            return objectMapper.readValue(json, EventDTO.class);
        }catch (Exception e){
            return null;
        }

    }


}
