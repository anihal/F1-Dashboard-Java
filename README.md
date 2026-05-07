# F1 Driver & Team Analytics Dashboard

**Author:** Nihal Ajayakumar - na4132

**GitHub:** https://github.com/anihal/F1-Dashboard-Java

---

## Overview

A desktop Java application that loads a Formula 1 historical dataset from local CSV files, parses and stores it in a local SQLite database, and presents an interactive JavaFX dashboard for exploring driver and team statistics from 1950 to 2020.

---

## Video Demo

[demo video — coming soon]

---

## Dependencies

| Dependency | Version | How it's managed |
|---|---|---|
| Java (JDK) | 17+ | Install manually (see below) |
| Maven | 3.6+ | Install manually (see below) |
| JavaFX | 21.0.2 | Downloaded automatically by Maven |
| sqlite-jdbc (`org.xerial:sqlite-jdbc`) | 3.45.3.0 | Downloaded automatically by Maven |
| JUnit Jupiter | 5.10.2 | Downloaded automatically by Maven (test only) |

Maven handles all library dependencies — you only need to install Java and Maven yourself. All other jars are fetched on first build.

No API key or internet connection required to run the app (Maven does need internet on the first build to download dependencies).

---

## Installing Java and Maven

### macOS (Homebrew)

```bash
brew install openjdk@17
brew install maven
```

If `java` is not found after install:

```bash
export JAVA_HOME=$(brew --prefix openjdk@17)
export PATH="$JAVA_HOME/bin:$PATH"
```

### Linux (apt)

```bash
sudo apt install openjdk-17-jdk maven
```

### Verify your install

```bash
java -version    # should print 17.x.x
mvn -version     # should print 3.6+
```

---

## How to Run

1. Clone or download this repository so that both `f1-dashboard/` and `formula-1-world-championship-1950-2020/` sit in the same parent folder (they already do if you downloaded the zip).

2. Open a terminal and navigate into the Java project:

   ```bash
   cd f1-dashboard
   ```

3. Run the application:

   ```bash
   mvn javafx:run
   ```

The first launch reads the CSV files from `../formula-1-world-championship-1950-2020/` and imports them into a local SQLite database (`f1_data.db`). A progress screen is shown during this step. Subsequent launches skip the import and start instantly.

---

## Dataset

**Formula 1 World Championship (1950–2020)** — sourced from [Kaggle](https://www.kaggle.com/datasets/rohanrao/formula-1-world-championship-1950-2020).

The following CSV files are included in the `formula-1-world-championship-1950-2020/` folder and are required for the first-run import:

```
drivers.csv
constructors.csv
races.csv
results.csv
driver_standings.csv
constructor_standings.csv
status.csv
```

---

## Advanced Topics Used

### 1. GUI — JavaFX

The entire user interface is built with JavaFX and FXML. The dashboard has three tabs — Drivers, Constructors, and Races — each with a searchable/filterable table, a stat card panel, and a live-updating chart. JavaFX property bindings keep the UI in sync with data without manual refresh calls. Charts used include a line chart (points per season) and a bar chart (wins per season).

Key classes: `MainController`, `DriverViewController`, `ConstructorViewController`, `RaceViewController`, `LoadingController`

### 2. Databases — JDBC + SQLite

On first launch, CSV data is loaded into a local SQLite database using the `org.xerial:sqlite-jdbc` driver. The schema has seven tables (`drivers`, `constructors`, `races`, `results`, `driver_standings`, `constructor_standings`, `status`) linked by foreign keys. Indexes are created on high-traffic columns (`driverId`, `raceId`, `constructorId`, `year`) for fast filtering. All user interactions (searching, selecting a driver, switching seasons) fire parameterized SQL queries through dedicated DAO classes.

Key classes: `DatabaseManager`, `DriverDao`, `ConstructorDao`, `RaceDao`

### 3. Multithreading

The CSV import runs on a background daemon thread using JavaFX's `Task<Void>` API so the UI stays responsive during the initial load. `Task` exposes `progressProperty` and `messageProperty`, which `LoadingController` binds directly to a `ProgressBar` and `Label` — live updates with no manual thread synchronization needed.

Key classes: `CsvImporter` (extends `Task<Void>`), `LoadingController`

### 4. File I/O

Each CSV file is read from disk using `BufferedReader` with `StandardCharsets.UTF_8` encoding. Lines are parsed manually, handling quoted fields and missing values. The import is wrapped in a single database transaction and committed atomically, so a failed import leaves no partial data.

Key class: `CsvImporter`

---

## Features

- **Drivers tab** — search all drivers, filter by season, view stat cards (races, wins, podiums, poles, points, championships), and a points-per-season line chart
- **Constructors tab** — same layout for constructor teams with a wins-per-season bar chart
- **Races tab** — select a season to browse every round; click a race to see full results with podium highlighting (gold / silver / bronze rows)
