module com.example.uhlikova_stopa {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    opens com.example.uhlikova_stopa to javafx.graphics;
    opens com.example.uhlikova_stopa.controllers to javafx.fxml;
}
