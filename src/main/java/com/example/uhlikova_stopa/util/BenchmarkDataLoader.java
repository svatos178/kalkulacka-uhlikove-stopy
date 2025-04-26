package com.example.uhlikova_stopa.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class BenchmarkDataLoader {

    private static final String BENCHMARK_JSON_PATH = "/com/example/uhlikova_stopa/config/benchmark_data.json";
    private static JsonNode data;

    static {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = BenchmarkDataLoader.class.getResourceAsStream(BENCHMARK_JSON_PATH)) {
            if (is == null) {
                throw new RuntimeException("benchmark_data.json nebyl nalezen!");
            }
            data = mapper.readTree(is);
        } catch (IOException e) {
            throw new RuntimeException("Chyba při načítání benchmark_data.json", e);
        }
    }

    public static JsonNode getCRData() {
        return data.path("CR");
    }

    public static JsonNode getEUData() {
        return data.path("EU");
    }
}
