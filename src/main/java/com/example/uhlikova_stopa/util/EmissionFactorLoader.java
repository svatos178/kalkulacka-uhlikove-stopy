package com.example.uhlikova_stopa.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class EmissionFactorLoader {

    private static final String FACTORS_JSON_PATH = "/com/example/uhlikova_stopa/config/emission_factors.json";
    private static JsonNode data;

    static {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = EmissionFactorLoader.class.getResourceAsStream(FACTORS_JSON_PATH)) {
            if (is == null) {
                throw new RuntimeException("emission_factors.json nebyl nalezen!");
            }
            data = mapper.readTree(is);
        } catch (IOException e) {
            throw new RuntimeException("Chyba při načítání emission_factors.json", e);
        }
    }

    public static double getEnergyFactor(String key) {
        return data.path("energy").path(key).asDouble();
    }

    public static double getTravelFactor(String key) {
        return data.path("travel").path(key).asDouble();
    }

    public static double getWasteFactor(String material, String method) {
        JsonNode wasteNode = data.path("waste");
        if (wasteNode.has(material) && wasteNode.get(material).has(method)) {
            return wasteNode.get(material).get(method).asDouble();
        }
        throw new IllegalArgumentException("Emission factor pro " + material + " a metodu " + method + " nebyl nalezen.");
    }

    public static double getAgricultureFactor(String key) {
        JsonNode agriNode = data.path("agriculture");
        if (agriNode.has(key)) {
            return agriNode.get(key).asDouble();
        }
        throw new IllegalArgumentException("Emission factor pro " + key + " v agriculture sektoru nebyl nalezen.");
    }
}
