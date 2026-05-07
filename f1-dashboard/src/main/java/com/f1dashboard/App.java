package com.f1dashboard;

import com.f1dashboard.db.DatabaseManager;
import com.f1dashboard.ui.LoadingController;
import com.f1dashboard.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.nio.file.Path;

public class App extends Application {

    private static final String DATA_DIR = Path.of(
        System.getProperty("user.dir"), "..", "formula-1-world-championship-1950-2020"
    ).normalize().toString();

    private static final String DB_PATH = "f1_data.db";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadFonts();
        DatabaseManager db = new DatabaseManager(DB_PATH);
        db.initializeSchema();
        if (!db.isPopulated()) {
            showLoading(primaryStage, db);
        } else {
            showMain(primaryStage, db);
        }
    }

    private void showLoading(Stage stage, DatabaseManager db) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/f1dashboard/loading.fxml"));
        Scene scene = new Scene(loader.load(), 560, 220);
        applyStyles(scene);
        LoadingController ctrl = loader.getController();
        stage.setTitle("F1 Dashboard \u2014 Loading...");
        stage.setScene(scene);
        stage.show();
        ctrl.startImport(db, DATA_DIR, () -> {
            try { showMain(stage, db); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    private void showMain(Stage stage, DatabaseManager db) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/f1dashboard/main.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 700);
        applyStyles(scene);
        MainController ctrl = loader.getController();
        ctrl.initialize(db.getConnection());
        stage.setTitle("F1 Analytics Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void loadFonts() {
        String[] paths = {
            "/fonts/Inter-Regular.ttf", "/fonts/Inter-Medium.ttf",
            "/fonts/Inter-SemiBold.ttf", "/fonts/Inter-Bold.ttf",
            "/fonts/Rajdhani-Medium.ttf", "/fonts/Rajdhani-SemiBold.ttf",
            "/fonts/Rajdhani-Bold.ttf",
            "/fonts/JetBrainsMono-Regular.ttf", "/fonts/JetBrainsMono-Medium.ttf",
        };
        for (String p : paths) {
            var stream = getClass().getResourceAsStream(p);
            if (stream != null) Font.loadFont(stream, 12);
        }
    }

    private void applyStyles(Scene scene) {
        var css = getClass().getResource("/com/f1dashboard/application.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }
}
