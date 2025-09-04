package project.gui;

import javafx.geometry.Pos;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import project.map.RectangularGrassField;
import project.map.Vector2d;
import project.mapElements.Animal;
import project.mapElements.BigGrass;
import project.mapElements.Grass;
import project.mapElements.IMapElement;
import project.simulation.ISimulationStepObserver;
import project.simulation.SimulationEngine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;

import java.util.*;

import java.io.File;
import java.util.stream.Collectors;

public class App extends Application implements ISimulationStepObserver {

    private TabPane tabPane;
    private RectangularGrassField map;
    private GridPane grid;
    private Button startSimulationButton;
    private Button stepSimulationButton;
    private static Animal followedAnimal;
    private boolean dominantGenotypeHighlighted = false;
    private boolean preferredFieldsHighlighted = false;
    private String mostPopularGenome = ""; // String?

    Label animalCount; // modyfikator dostępu?
    Label simulationDay;
    Label grassCount;
    Label bigGrassCount;
    Label emptyFieldCount;
    Label averageLifeLength;
    Label totalDeadAnimals;
    Label mostPopularGenotype;
    Label averageEnergy;
    Label averageChildrenCount;

    private SimulationEngine engine;
    private File outputCSVFile;
    private SimulationPropertyFile simprop;

    private Map<Vector2d, IMapElement> currentGridState = new HashMap<>();

    public App() {
        //to jest naprawde potrzebne // a po co?
    }

    public App(SimulationPropertyFile simprop) {
        this.simprop = simprop;
    }

    @Override
    public void init() throws Exception {
        super.init();
        String[] args = getParameters().getRaw().toArray(new String[0]);
        File propertyFile = args.length > 0 ? new File(args[0]) : new File("config30.properties");

        outputCSVFile = new File(propertyFile.getParentFile(), propertyFile.getName() + "-output.csv");
        SimulationResults.writeHeaderToCSV(outputCSVFile);
        SimulationPropertyFile simulationProperty = new SimulationPropertyFile(propertyFile);

        map = new RectangularGrassField(simulationProperty.getIntValue(ESimulationProperty.szerokoscMapy),
                simulationProperty.getIntValue(ESimulationProperty.wysokoscMapy),
                simulationProperty.getIntValue(ESimulationProperty.startowaLiczbaRoslin));

        engine = new SimulationEngine(map, simulationProperty);
        engine.addObserver(this);
    }

    public static int GRID_WIDTH = 800; // atrybuty (publiczne, modyfikowalne) między metodami
    public static int GRID_HEIGHT = 800;

    private void initializeSimulation(SimulationPropertyFile propertyFile) throws Exception {
        outputCSVFile = new File("simulation-output.csv");
        SimulationResults.writeHeaderToCSV(outputCSVFile);

        map = new RectangularGrassField(
                propertyFile.getIntValue(ESimulationProperty.szerokoscMapy),
                propertyFile.getIntValue(ESimulationProperty.wysokoscMapy),
                propertyFile.getIntValue(ESimulationProperty.startowaLiczbaRoslin)
        );

        engine = new SimulationEngine(map, propertyFile);
        engine.addObserver(this);
    }

    private void showMainWindow(Stage primaryStage) {
        grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setMinSize(GRID_WIDTH, GRID_HEIGHT);

        GridPane controlPanel = createControlPane();
        HBox contents = new HBox(controlPanel, grid);

        Scene scene = new Scene(contents, 1200, 800);
        drawMap(map.getAnimals(), map.getGrassList(), map.getBigGrassList(), map.getWidth(), map.getHeight(), grid);

        primaryStage.setScene(scene);
        primaryStage.show();

        startSimulationButton.setOnAction(event -> startStopSimulation());
        stepSimulationButton.setOnAction(event -> stepForwardSimulation());
    }

    @Override
    public void start(Stage primaryStage) {
        Image appIcon = new Image(getClass().getResourceAsStream("/icon.png"));

        primaryStage.getIcons().add(appIcon);

        primaryStage.setTitle("Simulation Configuration");

        final SimulationMenu[] configMenuHolder = new SimulationMenu[1];
        configMenuHolder[0] = new SimulationMenu(() -> {
            try {
                Map<ESimulationProperty, String> config = configMenuHolder[0].getConfig();
                Properties props = new Properties();
                config.forEach((key, value) -> props.setProperty(key.toString(), value));
                SimulationPropertyFile propertyFile = new SimulationPropertyFile(props);

                Stage simulationStage = new Stage();

                simulationStage.getIcons().add(appIcon);

                simulationStage.setTitle("Darwin Game");

                App simulationApp = new App(propertyFile);

                simulationApp.initializeSimulation(propertyFile);
                simulationApp.showMainWindow(simulationStage);
            } catch (Exception e) { // catch co?
                e.printStackTrace();
            }
        }); // to jest za duże na lambdę
        configMenuHolder[0].show();
    }

    private static Label followedAnimalStatsLabel;

    private GridPane createControlPane() {
        startSimulationButton = new Button("Start");
        stepSimulationButton = new Button("Step");

        Button showDominantButton = new Button("Show Dominant Genotype");
        showDominantButton.setOnAction(event -> {
            dominantGenotypeHighlighted = !dominantGenotypeHighlighted;
            updateAnimalHighlights();
        });

        Button showPreferredButton = new Button("Show Preferred Fields");
        showPreferredButton.setOnAction(event -> {
            preferredFieldsHighlighted = !preferredFieldsHighlighted;
            updatePreferredFieldsHighlights();
        });

        simulationDay = new Label(".");
        animalCount = new Label(".");
        grassCount = new Label(".");
        bigGrassCount = new Label(".");
        emptyFieldCount = new Label(".");
        averageEnergy = new Label(".");
        averageLifeLength = new Label(".");
        totalDeadAnimals = new Label(".");
        mostPopularGenotype = new Label(".");
        averageChildrenCount = new Label(".");

        followedAnimalStatsLabel = new Label("Followed Animals Stats:\nNone");
        followedAnimalStatsLabel.setWrapText(true);

        GridPane controlPanel = new GridPane();

        int rowHeight = 30;
        int rowIndex = 0;

        controlPanel.add(startSimulationButton, 0, rowIndex);
        controlPanel.add(stepSimulationButton, 1, rowIndex);
        rowIndex++;
        controlPanel.add(showDominantButton, 0, rowIndex);
        controlPanel.add(showPreferredButton, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Simulation Day:"), 0, rowIndex);
        controlPanel.add(simulationDay, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Animal Count:"), 0, rowIndex);
        controlPanel.add(animalCount, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Grass Count:"), 0, rowIndex);
        controlPanel.add(grassCount, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Big Grass Count:"), 0, rowIndex);
        controlPanel.add(bigGrassCount, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Empty Field Count:"), 0, rowIndex);
        controlPanel.add(emptyFieldCount, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Average Energy:"), 0, rowIndex);
        controlPanel.add(averageEnergy, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Average Life Length:"), 0, rowIndex);
        controlPanel.add(averageLifeLength, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Number of dead animas:"), 0, rowIndex);
        controlPanel.add(totalDeadAnimals, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Most Popular Genotype:"), 0, rowIndex);
        controlPanel.add(mostPopularGenotype, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Average Children Count:"), 0, rowIndex);
        controlPanel.add(averageChildrenCount, 1, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));

        rowIndex++;
        controlPanel.add(new Label("Color Legend"), 0, rowIndex);
        controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));
        rowIndex++;

        for (int i = 0; i < 6; i++) {
            int energyThreshold = i * 15;
            String labelText;
            if (i < 5) {
                labelText = "energy < " + (energyThreshold + 15);
            } else {
                labelText = "energy >= " + energyThreshold;
            }

            controlPanel.add(new Label(labelText), 0, rowIndex + i);
            controlPanel.getRowConstraints().add(new RowConstraints(rowHeight));
            controlPanel.add(new Circle((double) rowHeight / 2, getColor(energyThreshold)), 1, rowIndex + i);
        }

        rowIndex += 6;
        controlPanel.add(followedAnimalStatsLabel, 0, rowIndex, 2, 1);

        controlPanel.setMinWidth(400);
        return controlPanel;
    } // ta metoda jest przerośnięta

    private void stepForwardSimulation() {
        engine.setPaused(true);
        startSimulationButton.setText("Start");

        Thread engineThread = new Thread(engine);
        engineThread.start();
    }

    private void startStopSimulation() {

        if (engine.isPaused()) {
            engine.setPaused(false);
            Thread engineThread = new Thread(engine);
            engineThread.start();
            startSimulationButton.setText("Pause");
        } else {
            engine.setPaused(true);
            startSimulationButton.setText("Start");
        }
    }

    private void updateAnimalHighlights() {
        if (map == null || grid == null) return;

        int height = map.getHeight();

        for (Animal animal : map.getAnimals()) {
            Vector2d position = animal.getPosition();
            int gridX = position.x + 1;
            int gridY = height - position.y;

            Node node = null;
            for (Node child : grid.getChildren()) {
                Integer childX = GridPane.getColumnIndex(child);
                Integer childY = GridPane.getRowIndex(child);
                if (childX != null && childY != null && childX == gridX && childY == gridY) {
                    node = child;
                    break;
                }
            }

            if (node != null && node instanceof VBox) {
                VBox container = (VBox) node;
                Node animalNode = container.getChildren().get(0);
                Rectangle energyBar = (Rectangle) container.getChildren().get(1);

                energyBar.setFill(getColor(animal.getEnergy()));

                if (animalNode instanceof StackPane) {
                    StackPane animalContainer = (StackPane) animalNode;
                    ImageView imageView = (ImageView) animalContainer.getChildren().get(0);

                    if (dominantGenotypeHighlighted && animal.getGenome().toString().equals(mostPopularGenome)) {
                        imageView.setEffect(new Glow(0.8));
                    } else {
                        imageView.setEffect(null);
                    }

                    if (animal.equals(followedAnimal)) {
                        animalContainer.setEffect(new DropShadow(10, Color.BLACK));
                    } else {
                        animalContainer.setEffect(null);
                    }
                }
            }
        }
    }

    private void updatePreferredFieldsHighlights() {
        if (map == null || grid == null) return;

        grid.getChildren().removeIf(node ->
                node instanceof Rectangle &&
                        ((Rectangle) node).getFill() == Color.LIGHTYELLOW
        );

        if (preferredFieldsHighlighted) {
            int height = map.getHeight();
            List<Vector2d> preferredFields = map.getPreferredEmptyFields();
            for (Vector2d position : preferredFields) {
                int gridX = position.x + 1;
                int gridY = height - position.y;

                Rectangle preferredField = new Rectangle(
                        GRID_WIDTH / (map.getWidth() + 1),
                        GRID_HEIGHT / (map.getHeight() + 1),
                        Color.LIGHTYELLOW
                );
                preferredField.setStroke(Color.DEEPPINK);
                preferredField.setStrokeWidth(2);

                preferredField.setViewOrder(-1);

                grid.add(preferredField, gridX, gridY);
            }

            if (simprop.getIntValue(ESimulationProperty.wariantWzrostuRoslin) == 1) {
                List<Vector2d> preferredBigFields = map.getPreferredBigGrassEmptyFields();

                for (Vector2d position : preferredBigFields) {
                    int gridX = position.x + 1;
                    int gridY = height - position.y;

                    Rectangle preferredBigField = new Rectangle(
                            GRID_WIDTH / (map.getWidth() + 1),
                            GRID_HEIGHT / (map.getHeight() + 1),
                            Color.LIGHTYELLOW
                    );
                    preferredBigField.setStroke(Color.LIGHTBLUE);
                    preferredBigField.setStrokeWidth(2);

                    preferredBigField.setViewOrder(-1);

                    grid.add(preferredBigField, gridX, gridY);
                }
            }


        }
    }

    private Node createNodeForElement(IMapElement element, int width, int height) {
        if (element instanceof Animal) {
            Animal animal = (Animal) element;

            Image animalImage = animal.getImage();

            ImageView imageView = new ImageView(animalImage);
            imageView.setFitWidth(Math.min(GRID_WIDTH / (width + 1), GRID_HEIGHT / (height + 1)));
            imageView.setFitHeight(Math.min(GRID_WIDTH / (width + 1), GRID_HEIGHT / (height + 1)));

            StackPane animalContainer = new StackPane();
            animalContainer.getChildren().add(imageView);

            if (dominantGenotypeHighlighted && animal.getGenome().toString().equals(mostPopularGenome)) {
                imageView.setEffect(new Glow(0.8));
            } else {
                imageView.setEffect(null);
            }

            if (animal.equals(followedAnimal)) {
                animalContainer.setEffect(new DropShadow(10, Color.BLACK));
            } else {
                animalContainer.setEffect(null);
            }

            animalContainer.setOnMouseClicked(event -> {
                if (followedAnimal != null && followedAnimal.equals(animal)) {
                    followedAnimal = null;
                } else {
                    followedAnimal = animal;
                }
                updateFollowedAnimalStats();
                updateAnimalHighlights();
            });

            VBox container = new VBox();
            container.setAlignment(Pos.CENTER);
            container.setSpacing(2);

            container.getChildren().add(animalContainer);

            Rectangle energyBar = new Rectangle(imageView.getFitWidth() * 0.8, 3, getColor(animal.getEnergy())); // Smaller bar
            container.getChildren().add(energyBar);

            return container;
        } else if (element instanceof Grass) {
            Image grassImage = new Image(getClass().getResourceAsStream("/grass.png"));
            ImageView grassView = new ImageView(grassImage);

            int cellSize = Math.min(GRID_WIDTH / (width + 1), GRID_HEIGHT / (height + 1));
            grassView.setFitWidth(cellSize);
            grassView.setFitHeight(cellSize);

            return grassView;
        } else if (element instanceof BigGrass) {
            Image bigGrassImage = new Image(getClass().getResourceAsStream("/biggrass.png"));
            ImageView bigGrassView = new ImageView(bigGrassImage);

            int cellSize = Math.min(GRID_WIDTH / (width + 1), GRID_HEIGHT / (height + 1));
            bigGrassView.setFitWidth(cellSize);
            bigGrassView.setFitHeight(cellSize);

            return bigGrassView;
        }

        return null;
    } // ta metoda jest przerośnięta

    private void drawGridCorners(int width, int height, GridPane grid) {
        int gridColumnCount = width + 1;
        int gridRowCount = height + 1;

        int columnWidth = GRID_WIDTH / gridColumnCount;
        int rowHeight = GRID_HEIGHT / gridRowCount;

        for (int x = 1; x < gridColumnCount - 1; x++) {
            Rectangle newRect = new Rectangle(columnWidth, rowHeight, Color.TRANSPARENT);
            grid.add(newRect, x, 1); // Top-left corner
        }

        for (int y = 1; y < gridRowCount - 1; y++) {
            Rectangle newRect = new Rectangle(columnWidth, rowHeight, Color.TRANSPARENT);
            grid.add(newRect, 1, y); // Top-left corner
        }
    }

    private void drawMap(List<Animal> animalList, List<Grass> grassList,
                         List<BigGrass> bigGrassList, int width, int height, GridPane grid) {
        Map<Vector2d, IMapElement> newGridState = new HashMap<>();
        drawGridCorners(width, height, grid);

        Set<BigGrass> previousBigGrass = currentGridState.values().stream()
                .filter(element -> element instanceof BigGrass)
                .map(element -> (BigGrass) element)
                .collect(Collectors.toSet());

        for (Grass grass : grassList) {
            newGridState.put(grass.getPosition(), grass);
        }

        Set<BigGrass> currentBigGrass = new HashSet<>();
        for (BigGrass bigGrass : bigGrassList) {
            currentBigGrass.add(bigGrass);
            for (Vector2d position : bigGrass.getCoveredPositions()) {
                newGridState.put(position, bigGrass);
            }
        }

        for (Animal animal : animalList) {
            newGridState.put(animal.getPosition(), animal);
        }

        Set<BigGrass> removedBigGrass = new HashSet<>(previousBigGrass);
        removedBigGrass.removeAll(currentBigGrass);
        for (BigGrass bigGrass : removedBigGrass) {
            for (Vector2d position : bigGrass.getCoveredPositions()) {
                grid.getChildren().removeIf(node -> {
                    Integer nodeX = GridPane.getColumnIndex(node);
                    Integer nodeY = GridPane.getRowIndex(node);
                    if (node instanceof Rectangle && ((Rectangle) node).getFill() == Color.LIGHTYELLOW) {
                        return false;
                    }
                    return nodeX != null && nodeY != null &&
                            nodeX == position.x + 1 &&
                            nodeY == height - position.y;
                });
            }
        }

        for (Map.Entry<Vector2d, IMapElement> entry : newGridState.entrySet()) {
            Vector2d position = entry.getKey();
            IMapElement element = entry.getValue();

            if (!element.equals(currentGridState.get(position))) {
                grid.getChildren().removeIf(node -> {
                    Integer nodeX = GridPane.getColumnIndex(node);
                    Integer nodeY = GridPane.getRowIndex(node);
                    if (node instanceof Rectangle && ((Rectangle) node).getFill() == Color.LIGHTYELLOW) {
                        return false;
                    }
                    return nodeX != null && nodeY != null &&
                            nodeX == position.x + 1 &&
                            nodeY == height - position.y;
                });

                Node node = createNodeForElement(element, width, height);
                grid.add(node, position.x + 1, height - position.y);
            }
        }

        for (Vector2d position : currentGridState.keySet()) {
            if (!newGridState.containsKey(position)) {
                grid.getChildren().removeIf(node -> {
                    Integer nodeX = GridPane.getColumnIndex(node);
                    Integer nodeY = GridPane.getRowIndex(node);
                    if (node instanceof Rectangle && ((Rectangle) node).getFill() == Color.LIGHTYELLOW) {
                        return false;
                    }
                    return nodeX != null && nodeY != null &&
                            nodeX == position.x + 1 &&
                            nodeY == height - position.y;
                });
            }
        }

        currentGridState = newGridState;

        if (preferredFieldsHighlighted) {
            updatePreferredFieldsHighlights();
        }
    } // ta metoda jest przerośnięta

    private static Paint getColor(int energy) {
        Paint color;
        if (energy < 15) {
            color = Color.RED;
        } else if (energy < 30) {
            color = Color.ORANGE;
        } else if (energy < 45) {
            color = Color.YELLOW;
        } else if (energy < 60) {
            color = Color.LIGHTGREEN;
        } else {
            color = Color.GREEN;
        }
        return color;
    }

    @Override
    public void stepCompleted(int completedStep) {
        if (map != null && grid != null) {
            int width = map.getWidth();
            int height = map.getHeight();
            List<Animal> animalList = map.getAnimals();
            List<Grass> grassList = map.getGrassList();
            List<BigGrass> bigGrassList = map.getBigGrassList();

            Platform.runLater(() -> {
                drawMap(animalList, grassList, bigGrassList, width, height, grid);
                updateAnimalHighlights();
                updatePreferredFieldsHighlights();
                if (preferredFieldsHighlighted) {
                    updatePreferredFieldsHighlights();
                }
            });

            SimulationResults simulationResults = this.createSimulationResult();
            this.mostPopularGenome = simulationResults.mostPopularGenome;

            Platform.runLater(() -> updateControlPane(simulationResults));

            simulationResults.writeLineToCSV(outputCSVFile);

            Platform.runLater(App::updateFollowedAnimalStats);
        }
    }

    private static void updateFollowedAnimalStats() {
        if (followedAnimal != null) {

            followedAnimalStatsLabel.setText("Followed Animal Stats:\n" +
                    "Genome: " + followedAnimal.getGenome() + "\n" +
                    "Active Genome: " + followedAnimal.getActiveGene() + "\n" +
                    "Energy: " + followedAnimal.getEnergy() + "\n" +
                    "Normal Plants Eaten: " + followedAnimal.getPlantsEaten() + "\n" +
                    "Big Plants Eaten: " + followedAnimal.getBigPlantsEaten() + "\n" +
                    "Children: " + followedAnimal.getChildrenCount() + "\n" +
                    "Descendants: " + followedAnimal.getDescendantsCount() + "\n" +
                    "Age: " + followedAnimal.getAge() + "\n" +
                    (followedAnimal.isAlive() ? "Alive" : "Died on day: " + followedAnimal.getDeathDay()));
        } else {
            followedAnimalStatsLabel.setText("Followed Animal Stats:\nNone");
        }
    }

    private void updateControlPane(SimulationResults simulationResults) {
        followedAnimalStatsLabel.setStyle("-fx-line-spacing: 10px;");

        simulationDay.setText("" + simulationResults.day);
        animalCount.setText("" + simulationResults.animalCount);
        grassCount.setText("" + simulationResults.grassCount);
        bigGrassCount.setText("" + simulationResults.bigGrassCount);
        emptyFieldCount.setText("" + simulationResults.emptyFieldCount);
        averageLifeLength.setText(simulationResults.averageLifeLength > 0 ?
                String.format("%.1f", simulationResults.averageLifeLength) : "-");
        totalDeadAnimals.setText("" + simulationResults.totalDeadAnimals);
        double avgEnergy = simulationResults.averageEnergy;
        averageEnergy.setText(avgEnergy > 0 ? String.format("%.1f", avgEnergy) : "-");
        mostPopularGenotype.setText(simulationResults.mostPopularGenomeCount > 0 ?
                simulationResults.mostPopularGenomeCount + " = " + simulationResults.mostPopularGenome : "-");
        double avgChildrenCount = simulationResults.averageChildrenCount;
        averageChildrenCount.setText(avgChildrenCount > 0 ? String.format("%.1f", avgChildrenCount) : "-");
    }

    public SimulationResults createSimulationResult() {
        SimulationResults simulationResults = new SimulationResults();

        simulationResults.day = engine.getCurrentStep();
        simulationResults.animalCount = map.getAnimals().size();
        simulationResults.grassCount = map.getGrassList().size();
        simulationResults.bigGrassCount = map.getBigGrassList().size();
        simulationResults.emptyFieldCount = map.getEmptyFieldCount();
        simulationResults.averageLifeLength = engine.averageLifeLength().orElse(-1);
        simulationResults.totalDeadAnimals = engine.totalDeadAnimal();
        simulationResults.averageEnergy = map.getAnimals().stream().mapToDouble(Animal::getEnergy).average().orElse(-1);
        simulationResults.averageChildrenCount = engine.averageChildrenCount();
        Map.Entry<String, Integer> mostPopularGene = map.getMostPopularGenes();
        if (mostPopularGene != null) {
            simulationResults.mostPopularGenomeCount = mostPopularGene.getValue();
            simulationResults.mostPopularGenome = mostPopularGene.getKey();
        } else {
            simulationResults.mostPopularGenomeCount = 0;
            simulationResults.mostPopularGenome = "";
        }
        return simulationResults;
    }
}
