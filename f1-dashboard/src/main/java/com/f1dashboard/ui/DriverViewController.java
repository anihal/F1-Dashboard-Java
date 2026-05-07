package com.f1dashboard.ui;

import com.f1dashboard.dao.DriverDao;
import com.f1dashboard.model.Driver;
import com.f1dashboard.model.DriverStats;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriverViewController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> yearFilter;
    @FXML private TableView<DriverStats> driverTable;
    @FXML private TableColumn<DriverStats, String>  colName;
    @FXML private TableColumn<DriverStats, String>  colNationality;
    @FXML private TableColumn<DriverStats, Integer> colRaces;
    @FXML private TableColumn<DriverStats, Integer> colWins;
    @FXML private TableColumn<DriverStats, Double>  colPoints;
    @FXML private Label detailName, detailNationality, detailDob, detailSeasons;
    @FXML private Label detailRaces, detailWins, detailPodiums, detailPoles, detailPoints, detailChampionships;
    @FXML private LineChart<String, Number> seasonChart;

    private DriverDao dao;

    public void initialize(Connection conn) throws Exception {
        dao = new DriverDao(conn);
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colRaces.setCellValueFactory(new PropertyValueFactory<>("totalRaces"));
        colWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));
        driverTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, sel) -> { if (sel != null) showDetail(sel); });

        List<String> years = new ArrayList<>();
        years.add("All");
        for (int y : dao.getAvailableYears()) years.add(String.valueOf(y));
        yearFilter.setItems(FXCollections.observableArrayList(years));
        yearFilter.setValue("All");
        yearFilter.setOnAction(e -> loadTopDrivers());

        loadTopDrivers();
    }

    private Integer getSelectedYear() {
        String val = yearFilter.getValue();
        return (val == null || val.equals("All")) ? null : Integer.parseInt(val);
    }

    private void loadTopDrivers() {
        try {
            SortedList<DriverStats> sorted = new SortedList<>(
                FXCollections.observableArrayList(dao.getTopDriversByWins(150, getSelectedYear())));
            sorted.comparatorProperty().bind(driverTable.comparatorProperty());
            driverTable.setItems(sorted);
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML private void onSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { loadTopDrivers(); return; }
        Integer year = getSelectedYear();
        try {
            List<Driver> drivers = dao.searchDrivers(q);
            List<DriverStats> stats = new ArrayList<>();
            for (Driver d : drivers) {
                DriverStats s = dao.getDriverStats(d.driverId, year);
                if (s != null) stats.add(s);
            }
            SortedList<DriverStats> sorted = new SortedList<>(FXCollections.observableArrayList(stats));
            sorted.comparatorProperty().bind(driverTable.comparatorProperty());
            driverTable.setItems(sorted);
        } catch (Exception e) { showError(e); }
    }

    @FXML private void onClear() {
        searchField.clear();
        loadTopDrivers();
    }

    private void showDetail(DriverStats s) {
        detailName.setText(s.fullName);
        try {
            detailNationality.setText(dao.getNationality(s.driverId));
            detailDob.setText(dao.getDob(s.driverId));
        } catch (Exception e) { /* ignore */ }
        detailSeasons.setText(s.firstSeason + " \u2013 " + s.lastSeason);
        detailRaces.setText(String.valueOf(s.totalRaces));
        detailWins.setText(String.valueOf(s.wins));
        detailPodiums.setText(String.valueOf(s.podiums));
        detailPoles.setText(String.valueOf(s.polePositions));
        detailPoints.setText(String.format("%.1f", s.totalPoints));
        detailChampionships.setText(String.valueOf(s.championships));
        updateChart(s.driverId);
    }

    private void updateChart(int driverId) {
        seasonChart.getData().clear();
        try {
            Map<Integer, Double> pts = dao.getPointsPerSeason(driverId);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Points");
            pts.forEach((yr, p) ->
                series.getData().add(new XYChart.Data<>(String.valueOf(yr), p)));
            seasonChart.getData().add(series);
        } catch (Exception e) { /* ignore */ }
    }

    private void showError(Exception e) {
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
    }
}
