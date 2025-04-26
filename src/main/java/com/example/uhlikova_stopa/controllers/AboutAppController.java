package com.example.uhlikova_stopa.controllers;

import com.example.uhlikova_stopa.SceneManager;
import com.example.uhlikova_stopa.SceneManagerAware;
import javafx.fxml.FXML;

public class AboutAppController implements SceneManagerAware {

    private SceneManager sceneManager;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    private void handleBackToMenu() {
        if (sceneManager != null) {
            sceneManager.switchScene("/com/example/uhlikova_stopa/start_menu.fxml", "Start Menu - Kalkulačka uhlíkové stopy");
        }
    }
}
