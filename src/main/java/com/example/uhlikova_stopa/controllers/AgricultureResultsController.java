package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.AgricultureData;
import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import com.example.uhlikova_stopa.util.BenchmarkDataLoader;
import com.example.uhlikova_stopa.util.EmissionFactorLoader;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import com.fasterxml.jackson.databind.JsonNode;

public class AgricultureResultsController implements SceneManagerAware {

    private SceneManager sceneManager;

    @FXML private Label totalEmissionsLabel;
    @FXML private Label perCapitaEmissionsLabel;
    @FXML private BarChart<String, Number> comparisonBarChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;
    @FXML private TextArea recommendationsArea;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        ApplicationState state = ApplicationState.getInstance();
        AgricultureData agricultureData = state.getAgricultureData();
        int population = state.getPopulation();

        double totalEmissions = agricultureData.getTotalEmissionsTons();
        totalEmissionsLabel.setText(String.format("%.2f t CO₂", totalEmissions));
        perCapitaEmissionsLabel.setText(String.format("%.2f t CO₂/osoba", totalEmissions / population));

        setupComparisonChart(agricultureData, population);
        generateRecommendations(agricultureData);
    }

    private void setupComparisonChart(AgricultureData data, int population) {
        comparisonBarChart.getData().clear();
        barChartXAxis.setLabel("Kategorie zemědělství");
        barChartYAxis.setLabel("t CO₂ na obyvatele");

        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("Vaše město");

        XYChart.Series<String, Number> crSeries = new XYChart.Series<>();
        crSeries.setName("ČR");

        XYChart.Series<String, Number> euSeries = new XYChart.Series<>();
        euSeries.setName("EU");

        JsonNode cr = BenchmarkDataLoader.getCRData();
        JsonNode eu = BenchmarkDataLoader.getEUData();


        double userCow = (data.getCows() * (EmissionFactorLoader.getAgricultureFactor("cow_enteric") + EmissionFactorLoader.getAgricultureFactor("cow_manure"))) / 1000.0 / population;
        double userSheep = (data.getSheep() * (EmissionFactorLoader.getAgricultureFactor("sheep_enteric") + EmissionFactorLoader.getAgricultureFactor("sheep_manure"))) / 1000.0 / population;
        double userPig = (data.getPigs() * EmissionFactorLoader.getAgricultureFactor("pig_manure")) / 1000.0 / population;
        double userPoultry = (data.getPoultry() * EmissionFactorLoader.getAgricultureFactor("poultry_manure")) / 1000.0 / population;
        double userFertilizer = (data.getFertilizer() * EmissionFactorLoader.getAgricultureFactor("synthetic_fertilizer_n2o")) / 1000.0 / population;

        userSeries.getData().add(new XYChart.Data<>("Krávy", userCow));
        userSeries.getData().add(new XYChart.Data<>("Ovce", userSheep));
        userSeries.getData().add(new XYChart.Data<>("Prasata", userPig));
        userSeries.getData().add(new XYChart.Data<>("Drůbež", userPoultry));
        userSeries.getData().add(new XYChart.Data<>("Hnojiva", userFertilizer));


        double crCow = (cr.get("cow_per_capita").asDouble() * (EmissionFactorLoader.getAgricultureFactor("cow_enteric") + EmissionFactorLoader.getAgricultureFactor("cow_manure"))) / 1000.0;
        double crSheep = (cr.get("sheep_per_capita").asDouble() * (EmissionFactorLoader.getAgricultureFactor("sheep_enteric") + EmissionFactorLoader.getAgricultureFactor("sheep_manure"))) / 1000.0;
        double crPig = (cr.get("pig_per_capita").asDouble() * EmissionFactorLoader.getAgricultureFactor("pig_manure")) / 1000.0;
        double crPoultry = (cr.get("poultry_per_capita").asDouble() * EmissionFactorLoader.getAgricultureFactor("poultry_manure")) / 1000.0;
        double crFert = (cr.get("fertilizer_kg_per_capita").asDouble() * EmissionFactorLoader.getAgricultureFactor("synthetic_fertilizer_n2o")) / 1000.0;

        crSeries.getData().add(new XYChart.Data<>("Krávy", crCow));
        crSeries.getData().add(new XYChart.Data<>("Ovce", crSheep));
        crSeries.getData().add(new XYChart.Data<>("Prasata", crPig));
        crSeries.getData().add(new XYChart.Data<>("Drůbež", crPoultry));
        crSeries.getData().add(new XYChart.Data<>("Hnojiva", crFert));

        double euCow = (eu.get("cow_per_capita").asDouble() * (EmissionFactorLoader.getAgricultureFactor("cow_enteric") + EmissionFactorLoader.getAgricultureFactor("cow_manure"))) / 1000.0;
        double euSheep = (eu.get("sheep_per_capita").asDouble() * (EmissionFactorLoader.getAgricultureFactor("sheep_enteric") + EmissionFactorLoader.getAgricultureFactor("sheep_manure"))) / 1000.0;
        double euPig = (eu.get("pig_per_capita").asDouble() * EmissionFactorLoader.getAgricultureFactor("pig_manure")) / 1000.0;
        double euPoultry = (eu.get("poultry_per_capita").asDouble() * EmissionFactorLoader.getAgricultureFactor("poultry_manure")) / 1000.0;
        double euFert = (eu.get("fertilizer_kg_per_capita").asDouble() * EmissionFactorLoader.getAgricultureFactor("synthetic_fertilizer_n2o")) / 1000.0;

        euSeries.getData().add(new XYChart.Data<>("Krávy", euCow));
        euSeries.getData().add(new XYChart.Data<>("Ovce", euSheep));
        euSeries.getData().add(new XYChart.Data<>("Prasata", euPig));
        euSeries.getData().add(new XYChart.Data<>("Drůbež", euPoultry));
        euSeries.getData().add(new XYChart.Data<>("Hnojiva", euFert));

        comparisonBarChart.getData().addAll(userSeries, crSeries, euSeries);
    }


    private void generateRecommendations(AgricultureData agricultureData) {
        StringBuilder rec = new StringBuilder();

        if (agricultureData.getCows() > 0.2 * ApplicationState.getInstance().getPopulation()) {
            rec.append("Zvažte snížení počtu krav nebo zlepšení pastvy pro snížení emisí CH₄.\n");
        }
        if (agricultureData.getPigs() > 0.7 * ApplicationState.getInstance().getPopulation()) {
            rec.append("Zvažte optimalizaci hospodaření s prasečím hnojem.\n");
        }
        if (agricultureData.getFertilizer() > 35 * ApplicationState.getInstance().getPopulation()) {
            rec.append("Snižte používání syntetických hnojiv a přejděte na organická hnojiva.\n");
        }
        if (rec.length() == 0) {
            rec.append("Vaše emise ze zemědělství jsou v normě.");
        }

        recommendationsArea.setText(rec.toString());
    }

    @FXML
    private void handleBackToMenu() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/start_menu.fxml", "Start Menu - Kalkulačka uhlíkové stopy");
        }
    }
    @FXML
    private void handleNextSection() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/summary_overview.fxml", "Shrnutí - Kalkulačka uhlíkové stopy");
        }
    }
}
