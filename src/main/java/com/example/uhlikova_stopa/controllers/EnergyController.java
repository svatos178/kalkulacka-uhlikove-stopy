package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.ApplicationState;
import com.example.uhlikova_stopa.EnergyData;
import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import com.example.uhlikova_stopa.util.EmissionFactorLoader;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class EnergyController implements SceneManagerAware {

    private SceneManager sceneManager;

    @FXML private TextField populationInput, hnedeUhliInput, cerneUhliInput,
            zemniPlynInput, obnovitelneZdrojeInput,
            tezkyTopnyOlejInput, lehkyTopnyOlejInput, lpgInput;

    @FXML private PieChart energyChart;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        applyNumericValidation();

        hnedeUhliInput.textProperty().addListener((obs, oldVal, newVal) -> updateChart());
        cerneUhliInput.textProperty().addListener((obs, oldVal, newVal) -> updateChart());
        zemniPlynInput.textProperty().addListener((obs, oldVal, newVal) -> updateChart());
        obnovitelneZdrojeInput.textProperty().addListener((obs, oldVal, newVal) -> updateChart());
        tezkyTopnyOlejInput.textProperty().addListener((obs, oldVal, newVal) -> updateChart());
        lehkyTopnyOlejInput.textProperty().addListener((obs, oldVal, newVal) -> updateChart());
        lpgInput.textProperty().addListener((obs, oldVal, newVal) -> updateChart());

        updateChart();
    }

    private void applyNumericValidation() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("[0-9]*\\.?[0-9]*") ? change : null;
        };


        for (TextField tf : new TextField[]{
                hnedeUhliInput, cerneUhliInput, zemniPlynInput, obnovitelneZdrojeInput,
                tezkyTopnyOlejInput, lehkyTopnyOlejInput, lpgInput, populationInput
        }) {
            tf.setTextFormatter(new TextFormatter<>(filter));
        }
    }

    @FXML
    private void handleCalculate() {
        try {
            double brownCoal = parseInputSafe(hnedeUhliInput);
            double blackCoal = parseInputSafe(cerneUhliInput);
            double naturalGas = parseInputSafe(zemniPlynInput);
            double renewables = parseInputSafe(obnovitelneZdrojeInput);
            double heavyFuelOil = parseInputSafe(tezkyTopnyOlejInput);
            double lightFuelOil = parseInputSafe(lehkyTopnyOlejInput);
            double lpg = parseInputSafe(lpgInput);
            int population = validatePopulationInput(populationInput);

            double totalEmissions =
                    brownCoal * EmissionFactorLoader.getEnergyFactor("brown_coal") +
                            blackCoal * EmissionFactorLoader.getEnergyFactor("black_coal") +
                            naturalGas * EmissionFactorLoader.getEnergyFactor("natural_gas") +
                            renewables * EmissionFactorLoader.getEnergyFactor("renewables") +
                            heavyFuelOil * EmissionFactorLoader.getEnergyFactor("heavy_fuel_oil") +
                            lightFuelOil * EmissionFactorLoader.getEnergyFactor("light_fuel_oil") +
                            lpg * EmissionFactorLoader.getEnergyFactor("lpg");

            ApplicationState state = ApplicationState.getInstance();
            state.setPopulation(population);

            EnergyData energyData = state.getEnergyData();
            energyData.setBrownCoal(brownCoal);
            energyData.setBlackCoal(blackCoal);
            energyData.setNaturalGas(naturalGas);
            energyData.setHeavyFuelOil(heavyFuelOil);
            energyData.setLightFuelOil(lightFuelOil);
            energyData.setLpg(lpg);
            energyData.setRenewables(renewables);
            energyData.setTotalEmissions(totalEmissions);

            sceneManager.switchScene("/com/example/uhlikova_stopa/energy_results.fxml", "Výsledky - Energetika");

        } catch (Exception e) {

        }
    }

    private double parseInputSafe(TextField input) {
        String text = input.getText().trim();
        return text.isEmpty() ? 0.0 : Double.parseDouble(text);
    }

    private int validatePopulationInput(TextField input) {
        String text = input.getText().trim();
        if (text.isEmpty()) {
            showErrorAlert("Chybějící údaj", "Zadejte prosím počet obyvatel.");
            throw new NumberFormatException("Počet obyvatel není zadán.");
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                showErrorAlert("Neplatný údaj", "Počet obyvatel musí být větší než 0.");
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            showErrorAlert("Neplatný vstup", "Počet obyvatel musí být kladné celé číslo.");
            input.clear();
            throw e;
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateChart() {
        double brownCoalVal = parseInputSafe(hnedeUhliInput);
        double blackCoalVal = parseInputSafe(cerneUhliInput);
        double naturalGasVal = parseInputSafe(zemniPlynInput);
        double renewablesVal = parseInputSafe(obnovitelneZdrojeInput);
        double heavyFuelOilVal = parseInputSafe(tezkyTopnyOlejInput);
        double lightFuelOilVal = parseInputSafe(lehkyTopnyOlejInput);
        double lpgVal = parseInputSafe(lpgInput);

        double brownCoalEm = brownCoalVal * EmissionFactorLoader.getEnergyFactor("brown_coal");
        double blackCoalEm = blackCoalVal * EmissionFactorLoader.getEnergyFactor("black_coal");
        double naturalGasEm = naturalGasVal * EmissionFactorLoader.getEnergyFactor("natural_gas");
        double renewablesEm = renewablesVal * EmissionFactorLoader.getEnergyFactor("renewables");
        double heavyFuelOilEm = heavyFuelOilVal * EmissionFactorLoader.getEnergyFactor("heavy_fuel_oil");
        double lightFuelOilEm = lightFuelOilVal * EmissionFactorLoader.getEnergyFactor("light_fuel_oil");
        double lpgEm = lpgVal * EmissionFactorLoader.getEnergyFactor("lpg");

        double total = brownCoalEm + blackCoalEm + naturalGasEm + renewablesEm +
                heavyFuelOilEm + lightFuelOilEm + lpgEm;

        energyChart.getData().clear();

        if (total > 0) {
            addSlice("Hnědé uhlí", brownCoalEm, total);
            addSlice("Černé uhlí", blackCoalEm, total);
            addSlice("Zemní plyn", naturalGasEm, total);
            addSlice("Obnovitelné zdroje", renewablesEm, total);
            addSlice("Těžký topný olej", heavyFuelOilEm, total);
            addSlice("Lehký topný olej", lightFuelOilEm, total);
            addSlice("LPG", lpgEm, total);
        }
    }

    private void addSlice(String label, double value, double total) {
        if (value <= 0) return;
        double percent = (value / total) * 100.0;
        String sliceLabel = String.format("%s (%.1f%%)", label, percent);
        energyChart.getData().add(new PieChart.Data(sliceLabel, value));
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
