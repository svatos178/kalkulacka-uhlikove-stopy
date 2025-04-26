package com.example.uhlikova_stopa;

public class EnergyData {

    private double brownCoal;
    private double blackCoal;
    private double naturalGas;
    private double heavyFuelOil;
    private double lightFuelOil;
    private double lpg;
    private double renewables;
    private double totalEmissions;

    public double getBrownCoal() {
        return brownCoal;
    }

    public void setBrownCoal(double brownCoal) {
        this.brownCoal = brownCoal;
    }

    public double getBlackCoal() {
        return blackCoal;
    }

    public void setBlackCoal(double blackCoal) {
        this.blackCoal = blackCoal;
    }

    public double getNaturalGas() {
        return naturalGas;
    }

    public void setNaturalGas(double naturalGas) {
        this.naturalGas = naturalGas;
    }

    public double getHeavyFuelOil() {
        return heavyFuelOil;
    }

    public void setHeavyFuelOil(double heavyFuelOil) {
        this.heavyFuelOil = heavyFuelOil;
    }

    public double getLightFuelOil() {
        return lightFuelOil;
    }

    public void setLightFuelOil(double lightFuelOil) {
        this.lightFuelOil = lightFuelOil;
    }

    public double getLpg() {
        return lpg;
    }

    public void setLpg(double lpg) {
        this.lpg = lpg;
    }

    public double getRenewables() {
        return renewables;
    }

    public void setRenewables(double renewables) {
        this.renewables = renewables;
    }

    public double getTotalEmissions() {
        return totalEmissions;
    }

    public void setTotalEmissions(double totalEmissions) {
        this.totalEmissions = totalEmissions;
    }
}