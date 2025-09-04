package project.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SimulationMenu extends Stage {
    private Map<ESimulationProperty, TextField> inputFields = new HashMap<>();
    private CheckBox saveStatisticsCheckbox;
    private ComboBox<String> plantsVariantCombo;
    private ComboBox<String> behaviorVariantCombo;

    // Konstruktor menu symulacji, przyjmujący funkcję uruchamiającą symulację
    public SimulationMenu(Runnable onStartSimulation) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Dodanie pól dla podstawowych właściwości symulacji
        addField(grid, 0, "Width:", ESimulationProperty.szerokoscMapy);
        addField(grid, 1, "Height:", ESimulationProperty.wysokoscMapy);
        addField(grid, 2, "Initial Grass:", ESimulationProperty.startowaLiczbaRoslin);
        addField(grid, 3, "Initial Animals:", ESimulationProperty.startowaLiczbaZwierzakow);
        addField(grid, 4, "Animal Start Energy:", ESimulationProperty.startowaEnergiaZwierzakow);
        addField(grid, 5, "Energy From Plant:", ESimulationProperty.energiaZapewnianaPrzezZjedzenieJednejRosliny);
        addField(grid, 6, "Plant Spawn Rate:", ESimulationProperty.liczbaRoslinWyrastajacaKazdegoDnia);
        addField(grid, 7, "Reproduce Energy:", ESimulationProperty.energiaZwierzakaGotowegoDoRozmnazania);
        addField(grid, 8, "Min Mutations:", ESimulationProperty.minimalnaLiczbaMutacji);
        addField(grid, 9, "Max Mutations:", ESimulationProperty.maksymalnaLiczbaMutacji);
        addField(grid, 10, "Genome Length:", ESimulationProperty.dlugoscGenomuZwierzakow);
        addField(grid, 11, "Move Duration (ms):", ESimulationProperty.animationStepDelay);
        addField(grid, 12, "Parent Energy:", ESimulationProperty.energiaRodzicowDoTworzeniaPotomka);
        addField(grid, 13, "Big Grass Energy:", ESimulationProperty.energiaZapewnianiaPrzezZjedzenieWielkiejRosliny);

        // Dodanie ComboBox dla wariantów zachowania i wariantów roślin
        plantsVariantCombo = new ComboBox<>();
        plantsVariantCombo.getItems().addAll("Zwykłe rośliny", "Dorodne Plony");
        plantsVariantCombo.setValue("Zwykłe rośliny");
        grid.add(new Label("Plants Variant:"), 0, 14);
        grid.add(plantsVariantCombo, 1, 14);

        behaviorVariantCombo = new ComboBox<>();
        behaviorVariantCombo.getItems().addAll("Pełna predestynacja", "Starość nie radość");
        behaviorVariantCombo.setValue("Pełna predestynacja");
        grid.add(new Label("Behavior Variant:"), 0, 15);
        grid.add(behaviorVariantCombo, 1, 15);

        // Dodanie CheckBox dla zapisywania statystyk
        saveStatisticsCheckbox = new CheckBox("Save Statistics");
        grid.add(saveStatisticsCheckbox, 0, 16, 2, 1);

        // Dodanie przycisków Start, Load i Save
        Button startButton = new Button("Start Simulation");
        Button loadButton = new Button("Load Configuration");
        Button saveButton = new Button("Save Configuration");

        startButton.setOnAction(e -> {
            if (validateInputs()) {
                onStartSimulation.run();
            }
        });

        loadButton.setOnAction(e -> loadExampleData());
        saveButton.setOnAction(e -> saveConfiguration());


        grid.add(startButton, 0, 17);
        grid.add(loadButton, 1, 17);
        grid.add(saveButton, 2, 17);

        // Ustawienie sceny i pokazanie okna
        Scene scene = new Scene(grid, 500, 700);
        this.setTitle("Configure Simulation");
        this.setScene(scene);
    }

    // Zapisanie konfiguracji do pliku
    private void saveConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Configuration");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Properties Files", "*.properties"));
        File file = fileChooser.showSaveDialog(this);

        if (file != null) {
            Properties properties = new Properties();
            inputFields.forEach((key, value) -> properties.setProperty(key.toString(), value.getText()));
            properties.setProperty(ESimulationProperty.wariantWzrostuRoslin.toString(), plantsVariantCombo.getValue().equals("Normal Plants") ? "0" : "1");
            properties.setProperty(ESimulationProperty.wariantZachowaniaZwierzakow.toString(), behaviorVariantCombo.getValue().equals("Full Predestination") ? "1" : "2");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                properties.store(fos, "Simulation Configuration");
            } catch (IOException e) {
                showAlert("Error", "Failed to save configuration: " + e.getMessage());
            }
        }
    }

    // Wczytywanie przykładowych danych
    private void loadExampleData() {
        inputFields.get(ESimulationProperty.szerokoscMapy).setText("10");
        inputFields.get(ESimulationProperty.wysokoscMapy).setText("10");
        inputFields.get(ESimulationProperty.startowaLiczbaRoslin).setText("20");
        inputFields.get(ESimulationProperty.startowaLiczbaZwierzakow).setText("10");
        inputFields.get(ESimulationProperty.startowaEnergiaZwierzakow).setText("25");
        inputFields.get(ESimulationProperty.energiaZapewnianaPrzezZjedzenieJednejRosliny).setText("3");
        inputFields.get(ESimulationProperty.liczbaRoslinWyrastajacaKazdegoDnia).setText("5");
        inputFields.get(ESimulationProperty.energiaZwierzakaGotowegoDoRozmnazania).setText("5");
        inputFields.get(ESimulationProperty.minimalnaLiczbaMutacji).setText("1");
        inputFields.get(ESimulationProperty.maksymalnaLiczbaMutacji).setText("3");
        inputFields.get(ESimulationProperty.dlugoscGenomuZwierzakow).setText("5");
        inputFields.get(ESimulationProperty.animationStepDelay).setText("400");
        inputFields.get(ESimulationProperty.energiaRodzicowDoTworzeniaPotomka).setText("25");
        inputFields.get(ESimulationProperty.energiaZapewnianiaPrzezZjedzenieWielkiejRosliny).setText("10");

        plantsVariantCombo.setValue("Zwykłe rośliny");
        behaviorVariantCombo.setValue("Pełna predestynacja");
    }

    // Dodanie pola tekstowego do formularza
    private void addField(GridPane grid, int row, String label, ESimulationProperty property) {
        grid.add(new Label(label), 0, row);
        TextField textField = new TextField();
        inputFields.put(property, textField);
        grid.add(textField, 1, row);
    }

    // Dodanie pola tekstowego do formularza
    private boolean validateInputs() {
        try {
            for (Map.Entry<ESimulationProperty, TextField> entry : inputFields.entrySet()) {
                String value = entry.getValue().getText();
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Wypełnij wszystkie pola!");
                }
                int intValue = Integer.parseInt(value);
                if (intValue < 0) {
                    throw new IllegalArgumentException("Wartość dla " + entry.getKey() + " musi być dodatnia!");
                }
            }
            return true;
        } catch (NumberFormatException e) {
            showAlert("Błąd", "Wprowadź poprawne liczby!");
            return false;
        } catch (IllegalArgumentException e) {
            showAlert("Błąd", e.getMessage());
            return false;
        }
    }

    // Wyświetlenie okna dialogowego z komunikatem o błędzie
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Map<ESimulationProperty, String> getConfig() {
        Map<ESimulationProperty, String> config = new HashMap<>();

        inputFields.forEach((key, value) -> config.put(key, value.getText()));

        config.put(ESimulationProperty.wariantWzrostuRoslin, plantsVariantCombo.getValue().equals("Zwykłe rośliny") ? "0" : "1");
        config.put(ESimulationProperty.wariantZachowaniaZwierzakow, behaviorVariantCombo.getValue().equals("Pełna predestynacja") ? "1" : "2");

        return config;
    }
}