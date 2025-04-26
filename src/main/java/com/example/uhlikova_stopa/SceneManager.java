package com.example.uhlikova_stopa;

import com.example.uhlikova_stopa.SceneManagerAware;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private final Stage primaryStage;
    private final String defaultStylesheet = "/com/example/uhlikova_stopa/css/styles.css";
    private final int defaultWidth = 1920;
    private final int defaultHeight = 1080;

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void switchScene(String fxmlPath, String title) {
        try {
            System.out.println("switch na scenu: " + fxmlPath + title);


            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();


            Object controller = loader.getController();
            if (controller instanceof SceneManagerAware) {
                ((SceneManagerAware) controller).setSceneManager(this);
                System.out.println("SceneManager injectnut do : " + controller.getClass().getSimpleName());
            }


            Scene scene = new Scene(root, defaultWidth, defaultHeight);


            URL stylesheet = getClass().getResource(defaultStylesheet);
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet.toExternalForm());
            } else {
                System.err.println("Stylesheet not found at path: " + defaultStylesheet);
            }

            // Přepnutí scény
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error switching scenes: " + e.getMessage());
        }
    }
}
