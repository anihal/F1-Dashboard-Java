package com.f1dashboard.ui;

import com.f1dashboard.dao.ConstructorDao;
import com.f1dashboard.model.Constructor;
import com.f1dashboard.model.ConstructorStats;
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

public class ConstructorViewController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> yearFilter;
    @FXML private TableView<ConstructorStats> constructorTable;
    @FXML private TableColumn<ConstructorStats, String>  colName;
    @FXML private TableColumn<ConstructorStats, String>  colNationality;
    @FXML private TableColumn<ConstructorStats, Integer> colRaces;
    @FXML private TableColumn<ConstructorStats, Integer> colWins;
    @FXML private TableColumn<ConstructorStats, Double>  colPoints;
    @FXML private Label detailName, detailNationality, detailSeasons;
    @FXML private Label detailRaces, detailWins, detailPodiums, detailPoints, detailChampionships;
    @FXML private BarChart<String, Number> winsChart;

    private ConstructorDao dao;

    public void initialize(Connection conn) throws Exception {
        dao = new ConstructorDao(conn);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colRaces.setCellValueFactory(new PropertyValueFactory<>("totalRaces"));
        colWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));
        constructorTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, sel) -> { if (sel != null) showDetail(sel); });

        List<String> years = new ArrayList<>();
        years.add("All");
        for (int y = 2020; y >= 1950; y--) years.add(String.valueOf(y));
        yearFilter.setItems(FXCollections.observableArrayList(years));
        yearFilter.setValue("All");
        yearFilter.setOnAction(e -> loadTop());

        loadTop();
    }

    private Integer getSelectedYear() {
        String val = yearFilter.getValue();
        return (val == null || val.equals("All")) ? null : Integer.parseInt(val);
    }

    private void loadTop() {
        try {
            SortedList<ConstructorStats> sorted = new SortedList<>(
                FXCollections.observableArrayList(dao.getTopConstructorsByWins(50, getSelectedYear())));
            sorted.comparatorProperty().bind(constructorTable.comparatorProperty());
            constructorTable.setItems(sorted);
        } catch (Exception e) { showError(e); }
    }

    @FXML private void onSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { loadTop(); return; }
        Integer year = getSelectedYear();
        try {
            List<Constructor> constructors = dao.searchConstructors(q);
            List<ConstructorStats> stats = new ArrayList<>();
            for (Constructor c : constructors) {
                ConstructorStats s = dao.getConstructorStats(c.constructorId, year);
                if (s != null) stats.add(s);
            }
            SortedList<ConstructorStats> sorted = new SortedList<>(FXCollections.observableArrayList(stats));
            sorted.comparatorProperty().bind(constructorTable.comparatorProperty());
            constructorTable.setItems(sorted);
        } catch (Exception e) { showError(e); }
    }

    @FXML private void onClear() {
        searchField.clear();
        loadTop();
    }

    private void showDetail(ConstructorStats s) {
        detailName.setText(s.name);
        detailNationality.setText(s.nationality);
        detailSeasons.setText(s.firstSeason + " \u2013 " + s.lastSeason);
        detailRaces.setText(String.valueOf(s.totalRaces));
        detailWins.setText(String.valueOf(s.wins));
        detailPodiums.setText(String.valueOf(s.podiums));
        detailPoints.setText(String.format("%.1f", s.totalPoints));
        detailChampionships.setText(String.valueOf(s.championships));
        updateChart(s.constructorId);
    }

    private void updateChart(int constructorId) {
        winsChart.getData().clear();
        try {
            Map<Integer, Integer> wins = dao.getWinsPerSeason(constructorId);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Wins");
            wins.forEach((yr, w) ->
                series.getData().add(new XYChart.Data<>(String.valueOf(yr), w)));
            winsChart.getData().add(series);
        } catch (Exception e) { /* ignore */ }
    }

    private void showError(Exception e) {
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
    }
}
