package com.f1dashboard.ui;

import com.f1dashboard.db.CsvImporter;
import com.f1dashboard.db.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class LoadingController {

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    public void startImport(DatabaseManager db, String dataDir, Runnable onComplete) {
        CsvImporter task = new CsvImporter(dataDir, db);
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            onComplete.run();
        });
        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error: " + task.getException().getMessage());
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
