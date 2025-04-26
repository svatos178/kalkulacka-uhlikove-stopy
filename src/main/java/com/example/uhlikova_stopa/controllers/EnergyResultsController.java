package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.EnergyData;
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

public class EnergyResultsController implements SceneManagerAware {

    private SceneManager sceneManager;

    @FXML
    private Label totalEmissionsLabel;
    @FXML
    private Label perCapitaEmissionsLabel;
    @FXML
    private BarChart<String, Number> comparisonBarChart;
    @FXML
    private CategoryAxis barChartXAxis;
    @FXML
    private NumberAxis barChartYAxis;
    @FXML
    private TextArea recommendationsArea;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        ApplicationState state = ApplicationState.getInstance();
        EnergyData data = state.getEnergyData();
        double totalEmissions = data.getTotalEmissions();
        int population = state.getPopulation();

        totalEmissionsLabel.setText(String.format("%.2f t CO₂", totalEmissions));
        perCapitaEmissionsLabel.setText(String.format("%.2f t CO₂/osoba", totalEmissions / population));

        setupComparisonChart(state, population);
        generateRecommendations(data);
    }


    private void setupComparisonChart(ApplicationState state, int population) {
        comparisonBarChart.getData().clear();
        barChartXAxis.setLabel("Zdroj energie");
        barChartYAxis.setLabel("t CO₂ na obyvatele");

        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("Vaše město");

        XYChart.Series<String, Number> crSeries = new XYChart.Series<>();
        crSeries.setName("ČR");

        XYChart.Series<String, Number> euSeries = new XYChart.Series<>();
        euSeries.setName("EU");

        EnergyData data = state.getEnergyData();
        JsonNode cr = BenchmarkDataLoader.getCRData();
        JsonNode eu = BenchmarkDataLoader.getEUData();

        String[] keys = {"brown_coal", "black_coal", "natural_gas", "renewables", "heavy_fuel_oil", "light_fuel_oil", "lpg"};
        String[] labels = {"Hnědé uhlí", "Černé uhlí", "Zemní plyn", "Obnovitelné zdroje", "Těžký topný olej", "Lehký topný olej", "LPG"};

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String label = labels[i];
            double factor = EmissionFactorLoader.getEnergyFactor(key);


            double userVal = switch (key) {
                case "brown_coal" -> data.getBrownCoal();
                case "black_coal" -> data.getBlackCoal();
                case "natural_gas" -> data.getNaturalGas();
                case "renewables" -> data.getRenewables();
                case "heavy_fuel_oil" -> data.getHeavyFuelOil();
                case "light_fuel_oil" -> data.getLightFuelOil();
                case "lpg" -> data.getLpg();
                default -> 0;
            };
            userSeries.getData().add(new XYChart.Data<>(label, (userVal * factor) / population));


            double crVal = cr.get(key).asDouble();
            double crPop = cr.get("population").asDouble();
            crSeries.getData().add(new XYChart.Data<>(label, (crVal * factor) / crPop));


            double euVal = eu.get(key).asDouble();
            double euPop = eu.get("population").asDouble();
            euSeries.getData().add(new XYChart.Data<>(label, (euVal * factor) / euPop));
        }

        comparisonBarChart.getData().addAll(userSeries, crSeries, euSeries);
    }


    private void generateRecommendations(EnergyData data) {
        StringBuilder rec = new StringBuilder();
        double total = data.getTotalEmissions();

        if (total == 0) {
            rec.append("Nejsou k dispozici žádné údaje o spotřebě energie.\n");
            recommendationsArea.setText(rec.toString());
            return;
        }

        double brown = data.getBrownCoal() * EmissionFactorLoader.getEnergyFactor("brown_coal");
        double black = data.getBlackCoal() * EmissionFactorLoader.getEnergyFactor("black_coal");
        double gas = data.getNaturalGas() * EmissionFactorLoader.getEnergyFactor("natural_gas");
        double heavyOil = data.getHeavyFuelOil() * EmissionFactorLoader.getEnergyFactor("heavy_fuel_oil");
        double lightOil = data.getLightFuelOil() * EmissionFactorLoader.getEnergyFactor("light_fuel_oil");
        double renewables = data.getRenewables() * EmissionFactorLoader.getEnergyFactor("renewables");

        double oil = heavyOil + lightOil;
        double totalEnergyInput = data.getBrownCoal() + data.getBlackCoal() + data.getNaturalGas()
                + data.getHeavyFuelOil() + data.getLightFuelOil() + data.getLpg() + data.getRenewables();

        double brownShare = brown / total;
        double blackShare = black / total;
        double gasShare = gas / total;
        double renewablesShare = data.getRenewables() / totalEnergyInput;
        double oilShare = oil / total;

        if (renewablesShare < 0.15) {
            rec.append("Podíl obnovitelných zdrojů je nízký. Zvažte investice do OZE (solární, větrné, bioplyn...).\n");
        } else if (renewablesShare < 0.25) {
            rec.append("Podíl obnovitelných zdrojů je pod evropským průměrem. Zvažte jeho navýšení.\n");
        }

        if (brownShare > 0.2) {
            rec.append("Hnědé uhlí tvoří výraznou část emisí. Doporučujeme přechod na čistší zdroje energie.\n");
        }

        if (blackShare > 0.15) {
            rec.append("Podíl černého uhlí je nad doporučenými limity. Zvažte jeho omezení.\n");
        }

        if (oilShare > 0.1) {
            rec.append("Spotřeba topných olejů je vysoká. Přechod na nízkoemisní varianty může výrazně pomoci.\n");
        }

        if (gasShare > 0.25) {
            rec.append("Zemní plyn tvoří významnou část emisí. Přestože je čistší než uhlí, doporučuje se jeho postupné snižování.\n");
        }

        if (rec.length() == 0) {
            rec.append("Vaše spotřeba energie je v souladu s doporučenými limity. Dobrá práce!\n");
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
            sceneManager.switchScene("/com/example/uhlikova_stopa/sector_travel.fxml", "Doprava - Kalkulačka uhlíkové stopy");
        }
    }
}
