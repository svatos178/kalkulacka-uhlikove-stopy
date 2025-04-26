package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.WasteData;
import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import com.example.uhlikova_stopa.util.BenchmarkDataLoader;
import com.example.uhlikova_stopa.util.EmissionFactorLoader;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class WasteResultsController implements SceneManagerAware {

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
        WasteData data = state.getWasteData();
        int population = state.getPopulation();

        double totalEmissions = data.getTotalEmissionsTons();
        totalEmissionsLabel.setText(String.format("%.2f t CO₂", totalEmissions));
        perCapitaEmissionsLabel.setText(String.format("%.2f t CO₂/osoba", totalEmissions / population));

        setupComparisonChart(data, population);
        generateRecommendations(data);
    }

    private void setupComparisonChart(WasteData data, int population) {
        comparisonBarChart.getData().clear();
        barChartXAxis.setLabel("Kategorie odpadu");
        barChartYAxis.setLabel("t CO₂ na obyvatele");

        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("Vaše město");

        XYChart.Series<String, Number> crSeries = new XYChart.Series<>();
        crSeries.setName("ČR");

        XYChart.Series<String, Number> euSeries = new XYChart.Series<>();
        euSeries.setName("EU");

        JsonNode cr = BenchmarkDataLoader.getCRData();
        JsonNode eu = BenchmarkDataLoader.getEUData();

        addUserWasteData(userSeries, data, population);
        addBenchmarkWasteData(crSeries, cr);
        addBenchmarkWasteData(euSeries, eu);

        comparisonBarChart.getData().addAll(userSeries, crSeries, euSeries);
    }

    private void addUserWasteData(XYChart.Series<String, Number> series, WasteData data, int population) {
        addCategory(series, "Papír",
                data.getPaperRecycleKg(), data.getPaperLandfillKg(), data.getPaperIncinerationKg(),
                "paper", population);

        addCategory(series, "Plasty",
                data.getPlasticRecycleKg(), data.getPlasticLandfillKg(), data.getPlasticIncinerationKg(),
                "plastic", population);

        addCategory(series, "Sklo",
                data.getGlassRecycleKg(), data.getGlassLandfillKg(), data.getGlassIncinerationKg(),
                "glass", population);

        addCategory(series, "Kovy",
                data.getMetalRecycleKg(), data.getMetalLandfillKg(), data.getMetalIncinerationKg(),
                "metal", population);

        addCategory(series, "Bioodpad",
                data.getBioRecycleKg(), data.getBioLandfillKg(), data.getBioIncinerationKg(),
                "bio", population);

        addCategory(series, "Textil",
                data.getTextileRecycleKg(), data.getTextileLandfillKg(), data.getTextileIncinerationKg(),
                "textile", population);
    }

    private void addCategory(XYChart.Series<String, Number> series, String label,
                             double recycle, double landfill, double incineration,
                             String key, int population) {

        double totalKgCO2 =
                recycle * EmissionFactorLoader.getWasteFactor(key, "recycle") +
                        landfill * EmissionFactorLoader.getWasteFactor(key, "landfill") +
                        incineration * EmissionFactorLoader.getWasteFactor(key, "incineration");

        double tonsPerCapita = totalKgCO2 / 1000.0 / population;
        series.getData().add(new XYChart.Data<>(label, tonsPerCapita));
    }

    private void addBenchmarkWasteData(XYChart.Series<String, Number> series, JsonNode benchmark) {
        addBenchmark(series, "Papír", benchmark.get("paper_kg_per_capita").asDouble(), "paper");
        addBenchmark(series, "Plasty", benchmark.get("plastic_kg_per_capita").asDouble(), "plastic");
        addBenchmark(series, "Sklo", benchmark.get("glass_kg_per_capita").asDouble(), "glass");
        addBenchmark(series, "Kovy", benchmark.get("metal_kg_per_capita").asDouble(), "metal");
        addBenchmark(series, "Bioodpad", benchmark.get("bio_kg_per_capita").asDouble(), "bio");
        addBenchmark(series, "Textil", benchmark.get("textile_kg_per_capita").asDouble(), "textile");
    }

    private void addBenchmark(XYChart.Series<String, Number> series, String label, double kgPerCapita, String key) {
        double emissions = kgPerCapita * EmissionFactorLoader.getWasteFactor(key, "landfill"); // nejčastější scénář
        series.getData().add(new XYChart.Data<>(label, emissions / 1000.0));
    }

    private void generateRecommendations(WasteData data) {
        StringBuilder rec = new StringBuilder();
        int population = ApplicationState.getInstance().getPopulation();

        double paperTotal = data.getPaperRecycleKg() + data.getPaperLandfillKg() + data.getPaperIncinerationKg();
        double plasticTotal = data.getPlasticRecycleKg() + data.getPlasticLandfillKg() + data.getPlasticIncinerationKg();
        double glassTotal = data.getGlassRecycleKg() + data.getGlassLandfillKg() + data.getGlassIncinerationKg();
        double metalTotal = data.getMetalRecycleKg() + data.getMetalLandfillKg() + data.getMetalIncinerationKg();
        double bioTotal = data.getBioRecycleKg() + data.getBioLandfillKg() + data.getBioIncinerationKg();
        double textileTotal = data.getTextileRecycleKg() + data.getTextileLandfillKg() + data.getTextileIncinerationKg();

        double paperPerCapita = paperTotal / population;
        double plasticPerCapita = plasticTotal / population;
        double glassPerCapita = glassTotal / population;
        double metalPerCapita = metalTotal / population;
        double bioPerCapita = bioTotal / population;
        double textilePerCapita = textileTotal / population;


        if (paperPerCapita > 76) {
            rec.append("Produkce papírového odpadu je nad průměrem. Zvažte digitální řešení a recyklaci.\n");
        }
        if (plasticPerCapita > 36) {
            rec.append("Produkce plastového odpadu je vysoká. Omezte jednorázové plasty a zvyšte recyklaci.\n");
        }
        if (glassPerCapita > 35) {
            rec.append("Produkce skleněného odpadu je vyšší než průměr. Zvažte opakovaně použitelné obaly.\n");
        }
        if (metalPerCapita > 9) {
            rec.append("Kovový odpad překračuje běžné hodnoty. Dbejte na jeho správné třídění.\n");
        }
        if (bioPerCapita > 115) {
            rec.append("Produkce bioodpadu je vyšší než obvyklé množství. Zvažte domácí kompostování.\n");
        }
        if (textilePerCapita > 16) {
            rec.append("Textilní odpad přesahuje doporučené hodnoty. Podpořte sběr a opětovné využití textilu.\n");
        }

        if (rec.length() == 0) {
            rec.append("Nakládání s odpady odpovídá doporučeným normám. Pokračujte v udržitelném přístupu.");
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
            sceneManager.switchScene("/com/example/uhlikova_stopa/sector_agriculture.fxml", "Zemědělství - Kalkulačka uhlíkové stopy");
        }
    }
}
