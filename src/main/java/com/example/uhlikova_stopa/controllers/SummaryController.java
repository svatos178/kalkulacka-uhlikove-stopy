package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.*;
import com.example.uhlikova_stopa.util.EmissionFactorLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SummaryController implements SceneManagerAware {

    private SceneManager sceneManager;

    @FXML private PieChart sectorShareChart;
    @FXML private BarChart<String, Number> sectorBarChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;
    @FXML private Label totalEmissionsLabel;
    @FXML private Label totalMunicipalityLabel;
    @FXML private TextArea sectorInterpretationArea;
    @FXML private TextArea recommendationsArea;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        ApplicationState state = ApplicationState.getInstance();
        int population = state.getPopulation();

        double energy = state.getEnergyData().getTotalEmissions();
        double travel = state.getTravelData().getTotalEmissions();
        double waste = state.getWasteData().getTotalEmissionsTons();
        double agriculture = state.getAgricultureData().getTotalEmissionsTons();

        double energyPerCapita = energy / population;
        double travelPerCapita = travel / population;
        double wastePerCapita = waste / population;
        double agriculturePerCapita = agriculture / population;

        double total = energyPerCapita + travelPerCapita + wastePerCapita + agriculturePerCapita;
        double totalMunicipality = energy + travel + waste + agriculture;

        totalEmissionsLabel.setText(String.format("Celková uhlíková stopa na obyvatele: %.2f t CO₂/osoba", total));
        totalMunicipalityLabel.setText(String.format("Celková uhlíková stopa obce: %.0f t CO₂", totalMunicipality));

        buildPieChart(energyPerCapita, travelPerCapita, wastePerCapita, agriculturePerCapita);
        buildBarChart(energyPerCapita, travelPerCapita, wastePerCapita, agriculturePerCapita);
        buildInterpretation(state, population);
        buildRecommendations(energyPerCapita, travelPerCapita, wastePerCapita, agriculturePerCapita);
    }

    private void buildPieChart(double energy, double travel, double waste, double agriculture) {
        sectorShareChart.getData().clear();
        double sum = energy + travel + waste + agriculture;

        if (sum <= 0) return;

        sectorShareChart.getData().add(new PieChart.Data(String.format("Energetika (%.1f%%)", (energy / sum) * 100), energy));
        sectorShareChart.getData().add(new PieChart.Data(String.format("Doprava (%.1f%%)", (travel / sum) * 100), travel));
        sectorShareChart.getData().add(new PieChart.Data(String.format("Odpady (%.1f%%)", (waste / sum) * 100), waste));
        sectorShareChart.getData().add(new PieChart.Data(String.format("Zemědělství (%.1f%%)", (agriculture / sum) * 100), agriculture));
    }



    private void buildBarChart(double energy, double travel, double waste, double agriculture) {
        sectorBarChart.getData().clear();

        XYChart.Series<String, Number> energySeries = new XYChart.Series<>();
        energySeries.setName("Energetika");
        energySeries.getData().add(new XYChart.Data<>("Energetika", energy));

        XYChart.Series<String, Number> travelSeries = new XYChart.Series<>();
        travelSeries.setName("Doprava");
        travelSeries.getData().add(new XYChart.Data<>("Doprava", travel));

        XYChart.Series<String, Number> wasteSeries = new XYChart.Series<>();
        wasteSeries.setName("Odpady");
        wasteSeries.getData().add(new XYChart.Data<>("Odpady", waste));

        XYChart.Series<String, Number> agriSeries = new XYChart.Series<>();
        agriSeries.setName("Zemědělství");
        agriSeries.getData().add(new XYChart.Data<>("Zemědělství", agriculture));

        sectorBarChart.getData().addAll(energySeries, travelSeries, wasteSeries, agriSeries);
    }

    private void buildInterpretation(ApplicationState state, int population) {
        StringBuilder sb = new StringBuilder();

        sb.append("Přesný přehled emisí CO₂ za sektor (t celkem a kg/osoba):\n\n");

        EnergyData e = state.getEnergyData();
        double eTotal = e.getTotalEmissions();
        sb.append("Energetika (" + String.format("%.2f", eTotal) + " t / " + String.format("%.2f", (eTotal * 1000) / population) + " kg/osoba):\n");
        sb.append(String.format("- Hnědé uhlí: %.2f t (%.2f kg/osoba)\n", e.getBrownCoal() * EmissionFactorLoader.getEnergyFactor("brown_coal"), e.getBrownCoal() * EmissionFactorLoader.getEnergyFactor("brown_coal") * 1000 / population));
        sb.append(String.format("- Černé uhlí: %.2f t (%.2f kg/osoba)\n", e.getBlackCoal() * EmissionFactorLoader.getEnergyFactor("black_coal"), e.getBlackCoal() * EmissionFactorLoader.getEnergyFactor("black_coal") * 1000 / population));
        sb.append(String.format("- Zemní plyn: %.2f t (%.2f kg/osoba)\n", e.getNaturalGas() * EmissionFactorLoader.getEnergyFactor("natural_gas"), e.getNaturalGas() * EmissionFactorLoader.getEnergyFactor("natural_gas") * 1000 / population));
        sb.append(String.format("- Těžký topný olej: %.2f t (%.2f kg/osoba)\n", e.getHeavyFuelOil() * EmissionFactorLoader.getEnergyFactor("heavy_fuel_oil"), e.getHeavyFuelOil() * EmissionFactorLoader.getEnergyFactor("heavy_fuel_oil") * 1000 / population));
        sb.append(String.format("- Lehký topný olej: %.2f t (%.2f kg/osoba)\n", e.getLightFuelOil() * EmissionFactorLoader.getEnergyFactor("light_fuel_oil"), e.getLightFuelOil() * EmissionFactorLoader.getEnergyFactor("light_fuel_oil") * 1000 / population));
        sb.append(String.format("- LPG: %.2f t (%.2f kg/osoba)\n", e.getLpg() * EmissionFactorLoader.getEnergyFactor("lpg"), e.getLpg() * EmissionFactorLoader.getEnergyFactor("lpg") * 1000 / population));

        TravelData t = state.getTravelData();
        double tTotal = t.getTotalEmissions();
        sb.append("Doprava (" + String.format("%.2f", tTotal) + " t / " + String.format("%.2f", (tTotal * 1000) / population) + " kg/osoba):\n");
        sb.append(String.format("- Osobní auto (benzín): %.2f t (%.2f kg/osoba)\n", t.getPersonalCarPetrolKm() * EmissionFactorLoader.getTravelFactor("personal_car_petrol") / 1000.0, t.getPersonalCarPetrolKm() * EmissionFactorLoader.getTravelFactor("personal_car_petrol") / population));
        sb.append(String.format("- Osobní auto (nafta): %.2f t (%.2f kg/osoba)\n", t.getPersonalCarDieselKm() * EmissionFactorLoader.getTravelFactor("personal_car_diesel") / 1000.0, t.getPersonalCarDieselKm() * EmissionFactorLoader.getTravelFactor("personal_car_diesel") / population));
        sb.append(String.format("- Elektroauto: %.2f t (%.2f kg/osoba)\n", t.getPersonalCarElectricKm() * EmissionFactorLoader.getTravelFactor("personal_car_electric") / 1000.0, t.getPersonalCarElectricKm() * EmissionFactorLoader.getTravelFactor("personal_car_electric") / population));
        sb.append(String.format("- Autobusy: %.2f t (%.2f kg/osoba)\n", t.getBusKm() * EmissionFactorLoader.getTravelFactor("bus") / 1000.0, t.getBusKm() * EmissionFactorLoader.getTravelFactor("bus") / population));
        sb.append(String.format("- Nákladní auta: %.2f t (%.2f kg/osoba)\n", t.getTruckKm() * EmissionFactorLoader.getTravelFactor("truck") / 1000.0, t.getTruckKm() * EmissionFactorLoader.getTravelFactor("truck") / population));
        sb.append(String.format("- Vlaky: %.2f t (%.2f kg/osoba)\n\n", t.getTrainKm() * EmissionFactorLoader.getTravelFactor("train") / 1000.0, t.getTrainKm() * EmissionFactorLoader.getTravelFactor("train") / population));

        WasteData w = state.getWasteData();
        double wTotal = w.getTotalEmissionsTons();
        sb.append("Odpady (" + String.format("%.2f", wTotal) + " t / " + String.format("%.2f", (wTotal * 1000) / population) + " kg/osoba):\n");
        sb.append(String.format("- Papír : %.2f t (%.2f kg/osoba)\n", w.getPaperLandfillKg() * EmissionFactorLoader.getWasteFactor("paper", "landfill") / 1000.0, w.getPaperLandfillKg() * EmissionFactorLoader.getWasteFactor("paper", "landfill") / population));
        sb.append(String.format("- Plasty : %.2f t (%.2f kg/osoba)\n", w.getPlasticLandfillKg() * EmissionFactorLoader.getWasteFactor("plastic", "landfill") / 1000.0, w.getPlasticLandfillKg() * EmissionFactorLoader.getWasteFactor("plastic", "landfill") / population));
        sb.append(String.format("- Sklo : %.2f t (%.2f kg/osoba)\n", w.getGlassLandfillKg() * EmissionFactorLoader.getWasteFactor("glass", "landfill") / 1000.0, w.getGlassLandfillKg() * EmissionFactorLoader.getWasteFactor("glass", "landfill") / population));
        sb.append(String.format("- Kovy : %.2f t (%.2f kg/osoba)\n", w.getMetalLandfillKg() * EmissionFactorLoader.getWasteFactor("metal", "landfill") / 1000.0, w.getMetalLandfillKg() * EmissionFactorLoader.getWasteFactor("metal", "landfill") / population));
        sb.append(String.format("- Bioodpad : %.2f t (%.2f kg/osoba)\n", w.getBioLandfillKg() * EmissionFactorLoader.getWasteFactor("bio", "landfill") / 1000.0, w.getBioLandfillKg() * EmissionFactorLoader.getWasteFactor("bio", "landfill") / population));
        sb.append(String.format("- Textil : %.2f t (%.2f kg/osoba)\n\n", w.getTextileLandfillKg() * EmissionFactorLoader.getWasteFactor("textile", "landfill") / 1000.0, w.getTextileLandfillKg() * EmissionFactorLoader.getWasteFactor("textile", "landfill") / population));

        AgricultureData a = state.getAgricultureData();
        double aTotal = a.getTotalEmissionsTons();
        sb.append("Zemědělství (" + String.format("%.2f", aTotal) + " t / " + String.format("%.2f", (aTotal * 1000) / population) + " kg/osoba):\n");
        sb.append(String.format("- Krávy: %.2f t (%.2f kg/osoba)\n", a.getCows() * (EmissionFactorLoader.getAgricultureFactor("cow_enteric") + EmissionFactorLoader.getAgricultureFactor("cow_manure")), a.getCows() * (EmissionFactorLoader.getAgricultureFactor("cow_enteric") + EmissionFactorLoader.getAgricultureFactor("cow_manure")) * 1000 / population));
        sb.append(String.format("- Ovce: %.2f t (%.2f kg/osoba)\n", a.getSheep() * (EmissionFactorLoader.getAgricultureFactor("sheep_enteric") + EmissionFactorLoader.getAgricultureFactor("sheep_manure")), a.getSheep() * (EmissionFactorLoader.getAgricultureFactor("sheep_enteric") + EmissionFactorLoader.getAgricultureFactor("sheep_manure")) * 1000 / population));
        sb.append(String.format("- Prasata: %.2f t (%.2f kg/osoba)\n", a.getPigs() * EmissionFactorLoader.getAgricultureFactor("pig_manure"), a.getPigs() * EmissionFactorLoader.getAgricultureFactor("pig_manure") * 1000 / population));
        sb.append(String.format("- Drůbež: %.2f t (%.2f kg/osoba)\n", a.getPoultry() * EmissionFactorLoader.getAgricultureFactor("poultry_manure"), a.getPoultry() * EmissionFactorLoader.getAgricultureFactor("poultry_manure") * 1000 / population));
        sb.append(String.format("- Hnojiva: %.2f t (%.2f kg/osoba)\n", a.getFertilizer() * EmissionFactorLoader.getAgricultureFactor("synthetic_fertilizer_n2o") / 1000.0, a.getFertilizer() * EmissionFactorLoader.getAgricultureFactor("synthetic_fertilizer_n2o") / population));

        sectorInterpretationArea.setText(sb.toString());
    }

    private void buildRecommendations(double energy, double travel, double waste, double agriculture) {
        StringBuilder sb = new StringBuilder();
        double total = energy + travel + waste + agriculture;

        sb.append("Shrnutí klíčových doporučení na základě vašich údajů:\n\n");

        if (energy / total > 0.4)
            sb.append("- Energetika tvoří %.1f%% emisí. Zvažte podporu účinnějších zdrojů nebo OZE.\n".formatted((energy / total) * 100));
        if (travel / total > 0.4)
            sb.append("- Doprava tvoří %.1f%% emisí. Podpora MHD a bezemisních vozidel by mohla pomoci.\n".formatted((travel / total) * 100));
        if (waste / total > 0.3)
            sb.append("- Odpady tvoří %.1f%% emisí. Zvyšte recyklaci a snižte skládkování.\n".formatted((waste / total) * 100));
        if (agriculture / total > 0.3)
            sb.append("- Zemědělství tvoří %.1f%% emisí. Zvažte optimalizaci hospodaření se zvířaty a hnojivy.\n".formatted((agriculture / total) * 100));

        if (sb.toString().endsWith("\n\n"))
            sb.append("Všechny sektory jsou v doporučených mezích podle GPC.");

        recommendationsArea.setText(sb.toString());
    }

    @FXML
    private void handleBackToMenu() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/start_menu.fxml", "Start Menu - Kalkulačka uhlíkové stopy");
        }
    }





    @FXML
    private void handleExportJson() {
        ApplicationState state = ApplicationState.getInstance();
        Map<String, Object> exportData = new HashMap<>();

        int population = state.getPopulation();
        exportData.put("population", population);


        Map<String, Object> energyInputs = new HashMap<>();
        energyInputs.put("brownCoal", state.getEnergyData().getBrownCoal());
        energyInputs.put("blackCoal", state.getEnergyData().getBlackCoal());
        energyInputs.put("naturalGas", state.getEnergyData().getNaturalGas());
        energyInputs.put("renewables", state.getEnergyData().getRenewables());
        energyInputs.put("heavyFuelOil", state.getEnergyData().getHeavyFuelOil());
        energyInputs.put("lightFuelOil", state.getEnergyData().getLightFuelOil());
        energyInputs.put("lpg", state.getEnergyData().getLpg());

        Map<String, Object> energy = new HashMap<>();
        energy.put("inputs", energyInputs);
        energy.put("emissions", Map.of(
                "total", state.getEnergyData().getTotalEmissions(),
                "perCapita", state.getEnergyData().getTotalEmissions() / population
        ));


        Map<String, Object> travelInputs = new HashMap<>();
        travelInputs.put("personalCarPetrolKm", state.getTravelData().getPersonalCarPetrolKm());
        travelInputs.put("personalCarDieselKm", state.getTravelData().getPersonalCarDieselKm());
        travelInputs.put("personalCarElectricKm", state.getTravelData().getPersonalCarElectricKm());
        travelInputs.put("busKm", state.getTravelData().getBusKm());
        travelInputs.put("truckKm", state.getTravelData().getTruckKm());
        travelInputs.put("trainKm", state.getTravelData().getTrainKm());

        Map<String, Object> travel = new HashMap<>();
        travel.put("inputs", travelInputs);
        travel.put("emissions", Map.of(
                "total", state.getTravelData().getTotalEmissions(),
                "perCapita", state.getTravelData().getTotalEmissions() / population
        ));


        Map<String, Object> wasteInputs = new HashMap<>();
        wasteInputs.put("paperRecycleKg", state.getWasteData().getPaperRecycleKg());
        wasteInputs.put("paperIncinerationKg", state.getWasteData().getPaperIncinerationKg());
        wasteInputs.put("paperLandfillKg", state.getWasteData().getPaperLandfillKg());
        wasteInputs.put("plasticRecycleKg", state.getWasteData().getPlasticRecycleKg());
        wasteInputs.put("plasticIncinerationKg", state.getWasteData().getPlasticIncinerationKg());
        wasteInputs.put("plasticLandfillKg", state.getWasteData().getPlasticLandfillKg());
        wasteInputs.put("glassRecycleKg", state.getWasteData().getGlassRecycleKg());
        wasteInputs.put("glassIncinerationKg", state.getWasteData().getGlassIncinerationKg());
        wasteInputs.put("glassLandfillKg", state.getWasteData().getGlassLandfillKg());
        wasteInputs.put("metalRecycleKg", state.getWasteData().getMetalRecycleKg());
        wasteInputs.put("metalIncinerationKg", state.getWasteData().getMetalIncinerationKg());
        wasteInputs.put("metalLandfillKg", state.getWasteData().getMetalLandfillKg());
        wasteInputs.put("bioRecycleKg", state.getWasteData().getBioRecycleKg());
        wasteInputs.put("bioIncinerationKg", state.getWasteData().getBioIncinerationKg());
        wasteInputs.put("bioLandfillKg", state.getWasteData().getBioLandfillKg());
        wasteInputs.put("textileRecycleKg", state.getWasteData().getTextileRecycleKg());
        wasteInputs.put("textileIncinerationKg", state.getWasteData().getTextileIncinerationKg());
        wasteInputs.put("textileLandfillKg", state.getWasteData().getTextileLandfillKg());

        Map<String, Object> waste = new HashMap<>();
        waste.put("inputs", wasteInputs);
        waste.put("emissions", Map.of(
                "total", state.getWasteData().getTotalEmissionsTons(),
                "perCapita", state.getWasteData().getTotalEmissionsTons() / population
        ));


        Map<String, Object> agricultureInputs = new HashMap<>();
        agricultureInputs.put("cows", state.getAgricultureData().getCows());
        agricultureInputs.put("sheep", state.getAgricultureData().getSheep());
        agricultureInputs.put("pigs", state.getAgricultureData().getPigs());
        agricultureInputs.put("poultry", state.getAgricultureData().getPoultry());
        agricultureInputs.put("fertilizerKg", state.getAgricultureData().getFertilizer());

        Map<String, Object> agriculture = new HashMap<>();
        agriculture.put("inputs", agricultureInputs);
        agriculture.put("emissions", Map.of(
                "total", state.getAgricultureData().getTotalEmissionsTons(),
                "perCapita", state.getAgricultureData().getTotalEmissionsTons() / population
        ));

        Map<String, Object> sectors = new HashMap<>();
        sectors.put("energy", energy);
        sectors.put("travel", travel);
        sectors.put("waste", waste);
        sectors.put("agriculture", agriculture);

        exportData.put("sectors", sectors);
        exportData.put("totalEmissionsPerCapita",
                (state.getEnergyData().getTotalEmissions() +
                        state.getTravelData().getTotalEmissions() +
                        state.getWasteData().getTotalEmissionsTons() +
                        state.getAgricultureData().getTotalEmissionsTons()) / population
        );
        exportData.put("totalEmissionsMunicipality",
                state.getEnergyData().getTotalEmissions() +
                        state.getTravelData().getTotalEmissions() +
                        state.getWasteData().getTotalEmissionsTons() +
                        state.getAgricultureData().getTotalEmissionsTons()
        );

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Uložit JSON export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON soubory", "*.json"));
        fileChooser.setInitialFileName("export_uhlíkova_stopa.json");

        File selectedFile = fileChooser.showSaveDialog(new Stage());
        if (selectedFile != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(selectedFile, exportData);
                System.out.println("Export do JSON proběhl úspěšně: " + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Chyba při exportu do JSON: " + e.getMessage());
            }
        }
    }

}
