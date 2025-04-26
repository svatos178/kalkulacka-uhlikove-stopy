package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import javafx.fxml.FXML;

public class StartMenuController implements SceneManagerAware {

    private SceneManager sceneManager;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        System.out.println("SceneManager injectnut do " + this.getClass().getSimpleName());
    }

    @FXML
    private void handleCalculator() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/sector_energy.fxml", "Kalkulačka uhlíkové stopy - Energetika");
        }
    }

    @FXML
    private void handleAbout() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/about.fxml", "O aplikaci");
        }
    }

    @FXML
    private void handleHelp() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/help_view.fxml", "Nápověda");
        }
    }
}
