package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.AgricultureData;
import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import com.example.uhlikova_stopa.util.EmissionFactorLoader;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AgricultureController implements SceneManagerAware {

    private SceneManager sceneManager;

    @FXML private TextField cowsInput;
    @FXML private TextField sheepInput;
    @FXML private TextField pigsInput;
    @FXML private TextField poultryInput;
    @FXML private TextField fertilizerInput;
    @FXML private PieChart agricultureChart;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        enforceNumericInput(cowsInput);
        enforceNumericInput(sheepInput);
        enforceNumericInput(pigsInput);
        enforceNumericInput(poultryInput);
        enforceNumericInput(fertilizerInput);

        addListeners();
        updatePieChart();
    }

    private void enforceNumericInput(TextField input) {
        input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                input.setText(oldValue);
            }
        });
    }

    private void addListeners() {
        cowsInput.textProperty().addListener((obs, oldVal, newVal) -> updatePieChart());
        sheepInput.textProperty().addListener((obs, oldVal, newVal) -> updatePieChart());
        pigsInput.textProperty().addListener((obs, oldVal, newVal) -> updatePieChart());
        poultryInput.textProperty().addListener((obs, oldVal, newVal) -> updatePieChart());
        fertilizerInput.textProperty().addListener((obs, oldVal, newVal) -> updatePieChart());
    }

    private void updatePieChart() {
        agricultureChart.getData().clear();

        double cows = parseInputSafe(cowsInput);
        double sheep = parseInputSafe(sheepInput);
        double pigs = parseInputSafe(pigsInput);
        double poultry = parseInputSafe(poultryInput);
        double fertilizer = parseInputSafe(fertilizerInput);

        double cowEmissions = cows * (EmissionFactorLoader.getAgricultureFactor("cow_enteric") + EmissionFactorLoader.getAgricultureFactor("cow_manure"));
        double sheepEmissions = sheep * (EmissionFactorLoader.getAgricultureFactor("sheep_enteric") + EmissionFactorLoader.getAgricultureFactor("sheep_manure"));
        double pigEmissions = pigs * EmissionFactorLoader.getAgricultureFactor("pig_manure");
        double poultryEmissions = poultry * EmissionFactorLoader.getAgricultureFactor("poultry_manure");
        double fertilizerEmissions = fertilizer * EmissionFactorLoader.getAgricultureFactor("synthetic_fertilizer_n2o");

        double totalEmissions = cowEmissions + sheepEmissions + pigEmissions + poultryEmissions + fertilizerEmissions;

        if (totalEmissions <= 0) return;

        addPieSlice("Krávy", cowEmissions, totalEmissions);
        addPieSlice("Ovce", sheepEmissions, totalEmissions);
        addPieSlice("Prasata", pigEmissions, totalEmissions);
        addPieSlice("Drůbež", poultryEmissions, totalEmissions);
        addPieSlice("Syntetická hnojiva", fertilizerEmissions, totalEmissions);
    }

    private void addPieSlice(String label, double partial, double total) {
        if (partial <= 0) return;
        double percent = (partial / total) * 100.0;
        String sliceLabel = String.format("%s (%.1f%%)", label, percent);
        agricultureChart.getData().add(new PieChart.Data(sliceLabel, partial));
    }

    private double parseInputSafe(TextField input) {
        try {
            String text = input.getText().trim();
            return text.isEmpty() ? 0.0 : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @FXML
    private void handleBackToMenu() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/start_menu.fxml", "Start Menu - Kalkulačka uhlíkové stopy");
        }
    }

    @FXML
    private void handleCalculate() {
        try {
            ApplicationState state = ApplicationState.getInstance();
            AgricultureData data = state.getAgricultureData();

            double cows = parseInputSafe(cowsInput);
            double sheep = parseInputSafe(sheepInput);
            double pigs = parseInputSafe(pigsInput);
            double poultry = parseInputSafe(poultryInput);
            double fertilizer = parseInputSafe(fertilizerInput);

            double totalEmissions = (
                    cows * (EmissionFactorLoader.getAgricultureFactor("cow_enteric") + EmissionFactorLoader.getAgricultureFactor("cow_manure")) +
                            sheep * (EmissionFactorLoader.getAgricultureFactor("sheep_enteric") + EmissionFactorLoader.getAgricultureFactor("sheep_manure")) +
                            pigs * EmissionFactorLoader.getAgricultureFactor("pig_manure") +
                            poultry * EmissionFactorLoader.getAgricultureFactor("poultry_manure") +
                            fertilizer * EmissionFactorLoader.getAgricultureFactor("synthetic_fertilizer_n2o")
            ) / 1000.0;

            data.setCows(cows);
            data.setSheep(sheep);
            data.setPigs(pigs);
            data.setPoultry(poultry);
            data.setFertilizer(fertilizer);
            data.setTotalEmissionsTons(totalEmissions);

            sceneManager.switchScene("/com/example/uhlikova_stopa/agriculture_results.fxml", "Výsledky - Zemědělství");

        } catch (Exception e) {
            showErrorAlert("Chyba", "Nepodařilo se provést výpočet: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
