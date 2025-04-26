package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import com.example.uhlikova_stopa.util.EmissionFactorLoader;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class TravelController implements SceneManagerAware {

    private SceneManager sceneManager;

    @FXML
    private TextField personalCarPetrolInput, personalCarDieselInput, personalCarElectricInput;
    @FXML
    private TextField busInput, truckInput, trainInput;
    @FXML
    private PieChart travelChart;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    private void initialize() {
        addListeners();
        addNumericValidation(personalCarPetrolInput);
        addNumericValidation(personalCarDieselInput);
        addNumericValidation(personalCarElectricInput);
        addNumericValidation(busInput);
        addNumericValidation(truckInput);
        addNumericValidation(trainInput);
        updatePieChart();
    }


    private void addListeners() {
        personalCarPetrolInput.textProperty().addListener((observable, oldValue, newValue) -> updatePieChart());
        personalCarDieselInput.textProperty().addListener((observable, oldValue, newValue) -> updatePieChart());
        personalCarElectricInput.textProperty().addListener((observable, oldValue, newValue) -> updatePieChart());
        busInput.textProperty().addListener((observable, oldValue, newValue) -> updatePieChart());
        truckInput.textProperty().addListener((observable, oldValue, newValue) -> updatePieChart());
        trainInput.textProperty().addListener((observable, oldValue, newValue) -> updatePieChart());
    }

    @FXML
    private void handleCalculate() {
        try {
            double personalCarPetrolKm = validateInput(personalCarPetrolInput);
            double personalCarDieselKm = validateInput(personalCarDieselInput);
            double personalCarElectricKm = validateInput(personalCarElectricInput);
            double busKm = validateInput(busInput);
            double truckKm = validateInput(truckInput);
            double trainKm = validateInput(trainInput);

            double totalEmissions = (
                    (personalCarPetrolKm * EmissionFactorLoader.getTravelFactor("personal_car_petrol")) +
                            (personalCarDieselKm * EmissionFactorLoader.getTravelFactor("personal_car_diesel")) +
                            (personalCarElectricKm * EmissionFactorLoader.getTravelFactor("personal_car_electric")) +
                            (busKm * EmissionFactorLoader.getTravelFactor("bus")) +
                            (truckKm * EmissionFactorLoader.getTravelFactor("truck")) +
                            (trainKm * EmissionFactorLoader.getTravelFactor("train"))
            ) / 1000.0;

            ApplicationState state = ApplicationState.getInstance();
            state.getTravelData().setPersonalCarPetrolKm(personalCarPetrolKm);
            state.getTravelData().setPersonalCarDieselKm(personalCarDieselKm);
            state.getTravelData().setPersonalCarElectricKm(personalCarElectricKm);
            state.getTravelData().setBusKm(busKm);
            state.getTravelData().setTruckKm(truckKm);
            state.getTravelData().setTrainKm(trainKm);
            state.getTravelData().setTotalEmissions(totalEmissions);

            sceneManager.switchScene("/com/example/uhlikova_stopa/travel_results.fxml", "Výsledky - Doprava");

        } catch (Exception e) {
            showErrorAlert("Chyba", "Nepodařilo se provést výpočet: " + e.getMessage());
        }
    }

    private void updatePieChart() {
        travelChart.getData().clear();

        double personalCarPetrolKm = validateInputSafe(personalCarPetrolInput);
        double personalCarDieselKm = validateInputSafe(personalCarDieselInput);
        double personalCarElectricKm = validateInputSafe(personalCarElectricInput);
        double busKm = validateInputSafe(busInput);
        double truckKm = validateInputSafe(truckInput);
        double trainKm = validateInputSafe(trainInput);

        double totalEmissions = (
                (personalCarPetrolKm * EmissionFactorLoader.getTravelFactor("personal_car_petrol")) +
                        (personalCarDieselKm * EmissionFactorLoader.getTravelFactor("personal_car_diesel")) +
                        (personalCarElectricKm * EmissionFactorLoader.getTravelFactor("personal_car_electric")) +
                        (busKm * EmissionFactorLoader.getTravelFactor("bus")) +
                        (truckKm * EmissionFactorLoader.getTravelFactor("truck")) +
                        (trainKm * EmissionFactorLoader.getTravelFactor("train"))
        );

        if (totalEmissions <= 0) return;


        PieChart.Data[] slices = new PieChart.Data[] {
                createPieSlice("Osobní auto (benzín)", personalCarPetrolKm, "personal_car_petrol", totalEmissions),
                createPieSlice("Osobní auto (nafta)", personalCarDieselKm, "personal_car_diesel", totalEmissions),
                createPieSlice("Osobní auto (elektro)", personalCarElectricKm, "personal_car_electric", totalEmissions),
                createPieSlice("Autobusy", busKm, "bus", totalEmissions),
                createPieSlice("Nákladní auta", truckKm, "truck", totalEmissions),
                createPieSlice("Vlaky", trainKm, "train", totalEmissions)
        };

        for (PieChart.Data slice : slices) {
            if (slice != null) {
                travelChart.getData().add(slice);
            }
        }

        applyPieChartColors();
    }

    private PieChart.Data createPieSlice(String label, double km, String factorKey, double totalEmissions) {
        double partialEmission = km * EmissionFactorLoader.getTravelFactor(factorKey);
        if (partialEmission <= 0) {
            return null;
        }
        double percent = (partialEmission / totalEmissions) * 100.0;
        String sliceLabel = String.format("%s (%.1f%%)", label, percent);
        return new PieChart.Data(sliceLabel, partialEmission);
    }
    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void applyPieChartColors() {
        String[] colors = new String[]{
                "#FFA726",
                "#66BB6A",
                "#EF5350",
                "#29B6F6",
                "#AB47BC",
                "#FFCA28"
        };

        int i = 0;
        for (PieChart.Data data : travelChart.getData()) {
            String color = colors[i % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }


    private double validateInput(TextField input) throws NumberFormatException {
        String text = input.getText().trim();
        return text.isEmpty() ? 0.0 : Double.parseDouble(text);
    }

    private double validateInputSafe(TextField input) {
        try {
            return validateInput(input);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackToMenu() {
        try {
            sceneManager.switchScene("/com/example/uhlikova_stopa/start_menu.fxml", "Start Menu - Kalkulačka uhlíkové stopy");
        } catch (Exception e) {
            showErrorAlert("Chyba", "Nepodařilo se vrátit na hlavní menu.");
        }
    }
}
