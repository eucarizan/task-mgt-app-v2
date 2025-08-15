package dev.nj.tms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
