package com.example.uhlikova_stopa;

public class AgricultureData {

    private double cows;
    private double sheep;
    private double pigs;
    private double poultry;
    private double fertilizer;

    private double totalEmissionsTons;


    public void setCows(double cows) {
        this.cows = cows;
    }

    public void setSheep(double sheep) {
        this.sheep = sheep;
    }

    public void setPigs(double pigs) {
        this.pigs = pigs;
    }

    public void setPoultry(double poultry) {
        this.poultry = poultry;
    }

    public void setFertilizer(double fertilizer) {
        this.fertilizer = fertilizer;
    }

    public void setTotalEmissionsTons(double totalEmissionsTons) {
        this.totalEmissionsTons = totalEmissionsTons;
    }


    public double getCows() {
        return cows;
    }

    public double getSheep() {
        return sheep;
    }

    public double getPigs() {
        return pigs;
    }

    public double getPoultry() {
        return poultry;
    }

    public double getFertilizer() {
        return fertilizer;
    }

    public double getTotalEmissionsTons() {
        return totalEmissionsTons;
    }
}
