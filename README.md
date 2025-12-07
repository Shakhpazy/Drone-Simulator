# Autonomous Drone Simulator

A Java Swing application that simulates autonomous drone flight patterns, monitors real-time telemetry, and detects flight anomalies. The system includes a visual dashboard for monitoring and a database for logging and analyzing anomaly reports.

## Authors
* **Natan J. Artemiev**
* **Yusuf Shakhpaz**
* **Nathan Levin**
* **Evin Roen**

---

## Prerequisites
* **Java Development Kit (JDK) 21** or higher.
    * *Note: This project uses modern Java features (such as unnamed variables `_`). Older JDK versions will fail to compile.*
* **IntelliJ IDEA** (Highly Recommended).
* **JUnit 5** (for running tests).

---

## Installation & Setup (IntelliJ IDEA)

1.  **Clone or Download** this repository to your local machine.
2.  Open **IntelliJ IDEA** and select **File > Open**, then choose the project root folder.
3.  **Configure Dependencies**:
    * Go to **File > Project Structure > Modules**.
    * Select the **Dependencies** tab.
    * Click the **+** button and select **JARs or Directories**.
    * Navigate to the `lib/` folder in the project directory.
    * Select **all JAR files** inside (including SQLite, PDFBox, JUnit, etc.) and click **OK**.
    * Ensure they are checked in the dependencies list and click **Apply**.

---

## How to Run

### 1. Main Simulation (Standard Mode)
This is the primary application for running the simulation and dashboard.

1.  Navigate to `src/controller/DroneMonitorApp.java`.
2.  Click the green **Run** arrow (or right-click and select **Run 'DroneMonitorApp.main()'**).
3.  Enter the desired number of drones (1-200) when prompted.

### 2. Baseline Data Collection
Use this mode to collect telemetry data from "normal" drone behavior to establish statistical baselines.

1.  Navigate to `src/controller/ZScoreMonitor.java`.
2.  Run the class.
3.  This will log telemetry data and automatically run the `BaselineCalculator` upon completion.

### 3. Baseline Calculator Utility
If you have existing logs and need to re-calculate baseline statistics without running a new simulation:

1.  Navigate to `src/controller/RunBaseline.java`.
2.  Run the class.
3.  This reads from `dataLogs/TelemetryLog.txt` and exports stats to `dataLogs/BaselineLog.properties`.

## Command Line Note
*While running via an IDE is strongly recommended due to the multiple external dependencies, you can run from the command line if your classpath is configured correctly.*

**Unix/Mac Example:**

### Compile (assuming you are in the project root)
```bash
javac -cp "lib/*:src" -d out src/controller/DroneMonitorApp.java src/model/*.java src/view/*.java
```
### Run
```bash
java -cp "lib/*:out" controller.DroneMonitorApp
```
(For Windows, replace colons : with semicolons ; in the classpath)

## How to Use

### Monitor Dashboard
The dashboard allows you to visualize drone activity and view alerts in real-time.

* **Map Panel:** Shows the locations of drones on a latitude/longitude grid.
* **Telemetry Data:** Displays real-time data (velocity, battery, orientation) below the map.
    > **Tip:** Click on a specific data entry to highlight that drone on the map.
* **Anomaly Log:** Displays the most recent anomalies since the simulation started.
* **Details Panel:** Click on any log entry to view a detailed breakdown of that specific anomaly event.
* **Menu Bar:**
    * **File:** Save all reports to external files (PDF, CSV, JSON).
    * **Data:** Open the database query window.
    * **Help:** View detailed in-app instructions.

### Anomaly Database
The database window allows you to query historical anomaly reports.

* **Filter:** Use the provided fields to specify criteria (Drone ID, Anomaly Type, Date Range).
* **Query:** Click **GO** to fetch results from the database.
* **Export:** Select **File -> Save current selection as...** to export the query results to a file.

---

## How to Test
Unit tests are located in the `src/tests` directory and utilize JUnit 5.

1.  In IntelliJ, right-click the `src/tests` folder.
2.  Select **Run 'Tests in 'tests''**.
3.  Verify that all tests pass in the Run window.

---

## Project Structure
* `src/controller`: Main entry points and logic orchestration (DroneMonitorApp).
* `src/model`: Core logic for drones, telemetry, anomalies, and database management.
* `src/view`: Swing UI components (MonitorDashboard, MapPanel, etc.).
* `dataLogs/`: Stores generated telemetry logs and baseline properties.
* `lib/`: External dependencies (SQLite JDBC, PDFBox, JUnit, etc.).