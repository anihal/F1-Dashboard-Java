package com.f1dashboard.ui;

import com.f1dashboard.dao.RaceDao;
import com.f1dashboard.model.Race;
import com.f1dashboard.model.RaceResult;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.util.List;

public class RaceViewController {

    @FXML private ComboBox<Integer> yearPicker;
    @FXML private TableView<Race> raceTable;
    @FXML private TableColumn<Race, Integer> colRound;
    @FXML private TableColumn<Race, String> colRaceName;
    @FXML private TableColumn<Race, String> colDate;
    @FXML private TableView<RaceResult> resultsTable;
    @FXML private TableColumn<RaceResult, String> colPos;
    @FXML private TableColumn<RaceResult, String> colDriver;
    @FXML private TableColumn<RaceResult, String> colConstructor;
    @FXML private TableColumn<RaceResult, Integer> colLaps;
    @FXML private TableColumn<RaceResult, Double> colPoints;
    @FXML private TableColumn<RaceResult, String> colStatus;
    @FXML private Label selectedRaceLabel;
    @FXML private Label roundLabel;

    private RaceDao dao;

    public void initialize(Connection conn) throws Exception {
        dao = new RaceDao(conn);

        colRound.setCellValueFactory(new PropertyValueFactory<>("round"));
        colRaceName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        colPos.setCellValueFactory(new PropertyValueFactory<>("positionText"));
        colDriver.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        colConstructor.setCellValueFactory(new PropertyValueFactory<>("constructorName"));
        colLaps.setCellValueFactory(new PropertyValueFactory<>("laps"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));

        // Position-based row highlighting
        resultsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RaceResult item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("pos-p1", "pos-p2", "pos-p3", "pos-dnf");
                if (!empty && item != null) {
                    switch (item.positionOrder) {
                        case 1 -> getStyleClass().add("pos-p1");
                        case 2 -> getStyleClass().add("pos-p2");
                        case 3 -> getStyleClass().add("pos-p3");
                        default -> {
                            if (!item.positionText.matches("\\d+"))
                                getStyleClass().add("pos-dnf");
                        }
                    }
                }
            }
        });

        List<Integer> years = dao.getAvailableYears();
        yearPicker.setItems(FXCollections.observableArrayList(years));
        if (!years.isEmpty()) yearPicker.setValue(years.get(0));
        yearPicker.setOnAction(e -> loadRaces());

        raceTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, sel) -> {
                if (sel != null) {
                    selectedRaceLabel.setText(sel.name);
                    roundLabel.setText("ROUND " + sel.round);
                    loadResults(sel.raceId);
                }
            });

        loadRaces();
    }

    private void loadRaces() {
        Integer year = yearPicker.getValue();
        if (year == null) return;
        try {
            raceTable.setItems(FXCollections.observableArrayList(dao.getRacesByYear(year)));
            resultsTable.setItems(FXCollections.emptyObservableList());
            selectedRaceLabel.setText("Select a race");
            roundLabel.setText("ROUND —");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private void loadResults(int raceId) {
        try {
            resultsTable.setItems(FXCollections.observableArrayList(dao.getResultsForRace(raceId)));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
}
