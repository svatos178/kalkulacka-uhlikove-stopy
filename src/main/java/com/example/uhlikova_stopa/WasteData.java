package com.example.uhlikova_stopa;

public class WasteData {

    private double paperRecycleKg;
    private double paperLandfillKg;
    private double paperIncinerationKg;

    private double plasticRecycleKg;
    private double plasticLandfillKg;
    private double plasticIncinerationKg;

    private double glassRecycleKg;
    private double glassLandfillKg;
    private double glassIncinerationKg;

    private double metalRecycleKg;
    private double metalLandfillKg;
    private double metalIncinerationKg;

    private double bioRecycleKg;
    private double bioLandfillKg;
    private double bioIncinerationKg;

    private double textileRecycleKg;
    private double textileLandfillKg;
    private double textileIncinerationKg;

    private double totalEmissionsTons;


    public void setPaperData(double recycle, double landfill, double incineration) {
        this.paperRecycleKg = recycle;
        this.paperLandfillKg = landfill;
        this.paperIncinerationKg = incineration;
    }

    public void setPlasticData(double recycle, double landfill, double incineration) {
        this.plasticRecycleKg = recycle;
        this.plasticLandfillKg = landfill;
        this.plasticIncinerationKg = incineration;
    }

    public void setGlassData(double recycle, double landfill, double incineration) {
        this.glassRecycleKg = recycle;
        this.glassLandfillKg = landfill;
        this.glassIncinerationKg = incineration;
    }

    public void setMetalData(double recycle, double landfill, double incineration) {
        this.metalRecycleKg = recycle;
        this.metalLandfillKg = landfill;
        this.metalIncinerationKg = incineration;
    }

    public void setBioData(double recycle, double landfill, double incineration) {
        this.bioRecycleKg = recycle;
        this.bioLandfillKg = landfill;
        this.bioIncinerationKg = incineration;
    }

    public void setTextileData(double recycle, double landfill, double incineration) {
        this.textileRecycleKg = recycle;
        this.textileLandfillKg = landfill;
        this.textileIncinerationKg = incineration;
    }


    public double getPaperRecycleKg() { return paperRecycleKg; }
    public double getPaperLandfillKg() { return paperLandfillKg; }
    public double getPaperIncinerationKg() { return paperIncinerationKg; }

    public double getPlasticRecycleKg() { return plasticRecycleKg; }
    public double getPlasticLandfillKg() { return plasticLandfillKg; }
    public double getPlasticIncinerationKg() { return plasticIncinerationKg; }

    public double getGlassRecycleKg() { return glassRecycleKg; }
    public double getGlassLandfillKg() { return glassLandfillKg; }
    public double getGlassIncinerationKg() { return glassIncinerationKg; }

    public double getMetalRecycleKg() { return metalRecycleKg; }
    public double getMetalLandfillKg() { return metalLandfillKg; }
    public double getMetalIncinerationKg() { return metalIncinerationKg; }

    public double getBioRecycleKg() { return bioRecycleKg; }
    public double getBioLandfillKg() { return bioLandfillKg; }
    public double getBioIncinerationKg() { return bioIncinerationKg; }

    public double getTextileRecycleKg() { return textileRecycleKg; }
    public double getTextileLandfillKg() { return textileLandfillKg; }
    public double getTextileIncinerationKg() { return textileIncinerationKg; }

    public double getTotalEmissionsTons() {
        return totalEmissionsTons;
    }

    public void setTotalEmissionsTons(double totalEmissionsTons) {
        this.totalEmissionsTons = totalEmissionsTons;
    }
}
