package com.f1dashboard.ui;

import javafx.fxml.FXML;
import java.sql.Connection;

public class MainController {

    @FXML private DriverViewController driverViewIncludeController;
    @FXML private ConstructorViewController constructorViewIncludeController;
    @FXML private RaceViewController raceViewIncludeController;

    public void initialize(Connection conn) throws Exception {
        driverViewIncludeController.initialize(conn);
        constructorViewIncludeController.initialize(conn);
        raceViewIncludeController.initialize(conn);
    }
}
