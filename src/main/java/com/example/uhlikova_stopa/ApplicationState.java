package com.example.uhlikova_stopa;

public class ApplicationState {

    private static ApplicationState instance = null;
    private EnergyData energyData = new EnergyData();
    private TravelData travelData = new TravelData();
    private final WasteData wasteData = new WasteData();
    private AgricultureData agricultureData = new AgricultureData();
    private int population;


    public double getTotalEmissions() {
        return totalEmissions;
    }

    public void setTotalEmissions(double totalEmissions) {
        this.totalEmissions = totalEmissions;
    }


    private double totalEmissions;


    private ApplicationState() {}

    public static ApplicationState getInstance() {
        if (instance == null) {
            instance = new ApplicationState();
        }
        return instance;
    }


    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }





    public EnergyData getEnergyData() {
        return energyData;
    }
    public TravelData getTravelData() {
        return travelData;
    }
    public WasteData getWasteData() {
        return wasteData;
    }

    public AgricultureData getAgricultureData() {
        return agricultureData;
    }

}
