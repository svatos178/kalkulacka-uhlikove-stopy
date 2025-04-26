package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.TravelData;
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

public class TravelResultsController implements SceneManagerAware {

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
        double totalEmissions = state.getTravelData().getTotalEmissions();
        int population = state.getPopulation();

        totalEmissionsLabel.setText(String.format("%.2f t CO₂", totalEmissions));
        perCapitaEmissionsLabel.setText(String.format("%.2f t CO₂/osoba", totalEmissions / population));

        setupComparisonChart(state, population);
        generateRecommendations(state);
    }

    private void setupComparisonChart(ApplicationState state, int population) {
        comparisonBarChart.getData().clear();
        barChartXAxis.setLabel("Typ dopravy");
        barChartYAxis.setLabel("t CO₂ na obyvatele");
        barChartXAxis.setStyle("-fx-font-size: 14px;");
        barChartYAxis.setStyle("-fx-font-size: 14px;");

        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("Vaše město");

        XYChart.Series<String, Number> crSeries = new XYChart.Series<>();
        crSeries.setName("ČR");

        XYChart.Series<String, Number> euSeries = new XYChart.Series<>();
        euSeries.setName("EU");

        TravelData travelData = state.getTravelData();
        JsonNode crData = BenchmarkDataLoader.getCRData();
        JsonNode euData = BenchmarkDataLoader.getEUData();

        addUserData(userSeries, travelData, population);
        addBenchmarkData(crSeries, crData);
        addBenchmarkData(euSeries, euData);

        comparisonBarChart.getData().addAll(userSeries, crSeries, euSeries);
    }

    private void addUserData(XYChart.Series<String, Number> series, TravelData data, int population) {
        series.getData().add(new XYChart.Data<>("Osobní auto (benzín)", calculateEmissionPerCapita(data.getPersonalCarPetrolKm(), "personal_car_petrol", population)));
        series.getData().add(new XYChart.Data<>("Osobní auto (nafta)", calculateEmissionPerCapita(data.getPersonalCarDieselKm(), "personal_car_diesel", population)));
        series.getData().add(new XYChart.Data<>("Osobní auto (elektro)", calculateEmissionPerCapita(data.getPersonalCarElectricKm(), "personal_car_electric", population)));
        series.getData().add(new XYChart.Data<>("Autobusy", calculateEmissionPerCapita(data.getBusKm(), "bus", population)));
        series.getData().add(new XYChart.Data<>("Nákladní auta", calculateEmissionPerCapita(data.getTruckKm(), "truck", population)));
        series.getData().add(new XYChart.Data<>("Vlaky", calculateEmissionPerCapita(data.getTrainKm(), "train", population)));
    }

    private void addBenchmarkData(XYChart.Series<String, Number> series, JsonNode benchmark) {
        series.getData().add(new XYChart.Data<>("Osobní auto (benzín)", calculateBenchmarkEmissionDirect(benchmark.get("personal_car_petrol_km_per_year").asDouble(), "personal_car_petrol")));
        series.getData().add(new XYChart.Data<>("Osobní auto (nafta)", calculateBenchmarkEmissionDirect(benchmark.get("personal_car_diesel_km_per_year").asDouble(), "personal_car_diesel")));
        series.getData().add(new XYChart.Data<>("Osobní auto (elektro)", calculateBenchmarkEmissionDirect(benchmark.get("personal_car_electric_km_per_year").asDouble(), "personal_car_electric")));
        series.getData().add(new XYChart.Data<>("Autobusy", calculateBenchmarkEmissionDirect(benchmark.get("bus_km_per_year").asDouble(), "bus")));
        series.getData().add(new XYChart.Data<>("Nákladní auta", calculateBenchmarkEmissionDirect(benchmark.get("truck_km_per_year").asDouble(), "truck")));
        series.getData().add(new XYChart.Data<>("Vlaky", calculateBenchmarkEmissionDirect(benchmark.get("train_km_per_year").asDouble(), "train")));
    }

    private double calculateEmissionPerCapita(double km, String factorKey, int population) {
        return (km * EmissionFactorLoader.getTravelFactor(factorKey)) / 1000.0 / population;
    }

    private double calculateBenchmarkEmissionDirect(double kmPerCapita, String factorKey) {
        return (kmPerCapita * EmissionFactorLoader.getTravelFactor(factorKey)) / 1000.0;
    }

    private void generateRecommendations(ApplicationState state) {
        StringBuilder rec = new StringBuilder();
        TravelData travel = state.getTravelData();

        double petrolKm = travel.getPersonalCarPetrolKm();
        double dieselKm = travel.getPersonalCarDieselKm();
        double electricKm = travel.getPersonalCarElectricKm();
        double busKm = travel.getBusKm();
        double truckKm = travel.getTruckKm();
        double trainKm = travel.getTrainKm();

        if (petrolKm > 6000) {
            rec.append("Používání osobních aut na benzín je nad doporučenou hranicí 6 000 km/rok. Zvažte větší využití MHD nebo sdílené dopravy.\n");
        }
        if (dieselKm > 3000) {
            rec.append("Vysoký nájezd dieselových aut (limit 3 000 km/rok) může být problematický kvůli emisím NOx. Zvažte jejich omezení.\n");
        }
        if (electricKm < 1000) {
            rec.append("Podíl elektrovozidel je nízký. Zvažte podporu elektromobility pro nižší emise.\n");
        }
        if (busKm < 1200) {
            rec.append("Nízké využití autobusové dopravy. Doporučuje se zvýšit podporu a dostupnost MHD.\n");
        }
        if (truckKm > 1000) {
            rec.append("Nadměrné využití nákladní dopravy (> 1 000 km/rok) může znamenat vysoké emise. Zvažte alternativy jako železniční dopravu.\n");
        }
        if (trainKm < 400) {
            rec.append("Vlaky jsou ekologické, ale využití je nízké (< 400 km/rok). Podpořte jejich dostupnost.\n");
        }

        if (rec.length() == 0) {
            rec.append("Vaše hodnoty jsou v doporučených mezích. Pokračujte v podpoře udržitelných způsobů dopravy.\n");
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
            sceneManager.switchScene("/com/example/uhlikova_stopa/sector_waste.fxml", "Odpady - Kalkulačka uhlíkové stopy");
        }
    }
}
