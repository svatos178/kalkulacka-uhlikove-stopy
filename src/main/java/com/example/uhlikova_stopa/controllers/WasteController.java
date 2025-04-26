package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import com.example.uhlikova_stopa.WasteData;
import com.example.uhlikova_stopa.util.EmissionFactorLoader;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class WasteController implements SceneManagerAware {

    private SceneManager sceneManager;

    @FXML private TextField paperRecycleInput, paperLandfillInput, paperIncinerationInput;
    @FXML private TextField plasticRecycleInput, plasticLandfillInput, plasticIncinerationInput;
    @FXML private TextField glassRecycleInput, glassLandfillInput, glassIncinerationInput;
    @FXML private TextField metalRecycleInput, metalLandfillInput, metalIncinerationInput;
    @FXML private TextField bioRecycleInput, bioLandfillInput, bioIncinerationInput;
    @FXML private TextField textileRecycleInput, textileLandfillInput, textileIncinerationInput;
    @FXML private PieChart wasteChart;
    @FXML private Button nextButton;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        applyNumericValidation();
        addListeners();
        updatePieChart();
    }

    private void applyNumericValidation() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("[0-9]*\\.?[0-9]*") ? change : null;
        };

        for (TextField field : new TextField[]{
                paperRecycleInput, paperLandfillInput, paperIncinerationInput,
                plasticRecycleInput, plasticLandfillInput, plasticIncinerationInput,
                glassRecycleInput, glassLandfillInput, glassIncinerationInput,
                metalRecycleInput, metalLandfillInput, metalIncinerationInput,
                bioRecycleInput, bioLandfillInput, bioIncinerationInput,
                textileRecycleInput, textileLandfillInput, textileIncinerationInput
        }) {
            field.setTextFormatter(new TextFormatter<>(filter));
        }
    }

    private void addListeners() {
        TextField[] allInputs = {
                paperRecycleInput, paperLandfillInput, paperIncinerationInput,
                plasticRecycleInput, plasticLandfillInput, plasticIncinerationInput,
                glassRecycleInput, glassLandfillInput, glassIncinerationInput,
                metalRecycleInput, metalLandfillInput, metalIncinerationInput,
                bioRecycleInput, bioLandfillInput, bioIncinerationInput,
                textileRecycleInput, textileLandfillInput, textileIncinerationInput
        };

        for (TextField input : allInputs) {
            input.textProperty().addListener((observable, oldValue, newValue) -> updatePieChart());
        }
    }

    private void updatePieChart() {
        wasteChart.getData().clear();

        Map<String, Double> emissions = new HashMap<>();
        emissions.put("Papír", Math.max(0, calculateTotalCategoryEmissions("paper", paperRecycleInput, paperLandfillInput, paperIncinerationInput)));
        emissions.put("Plasty", Math.max(0, calculateTotalCategoryEmissions("plastic", plasticRecycleInput, plasticLandfillInput, plasticIncinerationInput)));
        emissions.put("Sklo", Math.max(0, calculateTotalCategoryEmissions("glass", glassRecycleInput, glassLandfillInput, glassIncinerationInput)));
        emissions.put("Kovy", Math.max(0, calculateTotalCategoryEmissions("metal", metalRecycleInput, metalLandfillInput, metalIncinerationInput)));
        emissions.put("Bioodpad", Math.max(0, calculateTotalCategoryEmissions("bio", bioRecycleInput, bioLandfillInput, bioIncinerationInput)));
        emissions.put("Textil", Math.max(0, calculateTotalCategoryEmissions("textile", textileRecycleInput, textileLandfillInput, textileIncinerationInput)));

        double totalEmissions = emissions.values().stream()
                .filter(value -> value > 0.001)
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalEmissions <= 0) {
            return;
        }

        for (Map.Entry<String, Double> entry : emissions.entrySet()) {
            double emission = entry.getValue();
            if (emission > 0.001) {
                double percent = (emission / totalEmissions) * 100.0;
                String sliceLabel = String.format("%s (%.1f%%)", entry.getKey(), percent);
                wasteChart.getData().add(new PieChart.Data(sliceLabel, emission));
            }
        }
    }

    private double calculateTotalCategoryEmissions(String type, TextField recycleInput, TextField landfillInput, TextField incinerationInput) {
        double recycle = parseInputSafe(recycleInput);
        double landfill = parseInputSafe(landfillInput);
        double incineration = parseInputSafe(incinerationInput);

        double emissionsKg = 0.0;
        emissionsKg += recycle * EmissionFactorLoader.getWasteFactor(type, "recycle");
        emissionsKg += landfill * EmissionFactorLoader.getWasteFactor(type, "landfill");
        emissionsKg += incineration * EmissionFactorLoader.getWasteFactor(type, "incineration");

        return emissionsKg / 1000.0;
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
            WasteData wasteData = state.getWasteData();

            wasteData.setPaperData(parseInputSafe(paperRecycleInput), parseInputSafe(paperLandfillInput), parseInputSafe(paperIncinerationInput));
            wasteData.setPlasticData(parseInputSafe(plasticRecycleInput), parseInputSafe(plasticLandfillInput), parseInputSafe(plasticIncinerationInput));
            wasteData.setGlassData(parseInputSafe(glassRecycleInput), parseInputSafe(glassLandfillInput), parseInputSafe(glassIncinerationInput));
            wasteData.setMetalData(parseInputSafe(metalRecycleInput), parseInputSafe(metalLandfillInput), parseInputSafe(metalIncinerationInput));
            wasteData.setBioData(parseInputSafe(bioRecycleInput), parseInputSafe(bioLandfillInput), parseInputSafe(bioIncinerationInput));
            wasteData.setTextileData(parseInputSafe(textileRecycleInput), parseInputSafe(textileLandfillInput), parseInputSafe(textileIncinerationInput));

            double total = 0.0;
            total += calculateTotalCategoryEmissions("paper", paperRecycleInput, paperLandfillInput, paperIncinerationInput);
            total += calculateTotalCategoryEmissions("plastic", plasticRecycleInput, plasticLandfillInput, plasticIncinerationInput);
            total += calculateTotalCategoryEmissions("glass", glassRecycleInput, glassLandfillInput, glassIncinerationInput);
            total += calculateTotalCategoryEmissions("metal", metalRecycleInput, metalLandfillInput, metalIncinerationInput);
            total += calculateTotalCategoryEmissions("bio", bioRecycleInput, bioLandfillInput, bioIncinerationInput);
            total += calculateTotalCategoryEmissions("textile", textileRecycleInput, textileLandfillInput, textileIncinerationInput);

            wasteData.setTotalEmissionsTons(total);

            sceneManager.switchScene("/com/example/uhlikova_stopa/waste_results.fxml", "Výsledky - Odpady");
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
