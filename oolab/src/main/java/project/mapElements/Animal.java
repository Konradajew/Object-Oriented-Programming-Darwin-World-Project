package project.mapElements;

import project.map.IWorldMap;
import project.map.MapDirection;
import project.map.Vector2d;
import project.gui.ESimulationProperty;
import project.gui.SimulationPropertyFile;
import javafx.scene.image.Image;

import java.util.*;

public class Animal extends AbstractMapElement {
    private static Random random = new Random();

    private UUID id = UUID.randomUUID();
    private IWorldMap map;
    public MapDirection orientation;
    final private List<Integer> genome;
    public int currentGeneIndex;
    private final int birthDay;
    private int energy;
    private int childrenCount = 0;
    private int age = 0;
    private int plantsEaten = 0;
    private int bigPlantsEaten = 0;
    private int deathDay = -1; // -1 oznacza, że zwierzę jeszcze nie umarło
    private Image image;
    private static final String IMAGE_FOLDER = "/";
    private static final int NUM_IMAGES = 10;


    private List<Animal> parents = new ArrayList<>();
    private List<Animal> children = new ArrayList<>();

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public List<Integer> getGenome() {
        return genome;
    }

    public Image getImage() {
        return image;
    }

    public int getPlantsEaten() {
        return plantsEaten;
    }

    public int getBigPlantsEaten() {
        return bigPlantsEaten;
    }

    public void incrementPlantsEaten() {
        plantsEaten++;
    }

    public void incrementBigPlantsEaten() {
        bigPlantsEaten++;
    }

    public UUID getId() {
        return id;
    }

    public int getDescendantsCount() {
        return getDescendantsCount(new HashSet<>());
    }
    // ta metoda private wykonuje całą "brudną robotę"
    private int getDescendantsCount(Set<UUID> visited) {
        int count = 0;
        for (Animal child : getChildren()) {
            if (visited.add(child.getId())) {
                count += 1 + child.getDescendantsCount(visited);
            }
        }
        return count;
    }

    public int getActiveGene() {
        return genome.get(currentGeneIndex);
    }

    public void setDeathDay(int deathDay) {
        this.deathDay = deathDay;
    }

    public int getDeathDay() {
        return deathDay;
    }

    public boolean isAlive() {
        return deathDay == -1;
    }

    public void addChild(Animal child) {
        this.children.add(child);
    }

    public List<Animal> getChildren() {
        return children;
    }

    private Image loadRandomImage() {
        int imageIndex = random.nextInt(NUM_IMAGES);
        String imagePath = IMAGE_FOLDER + "animal" + imageIndex + ".png";
        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        }
    }
    public Animal(IWorldMap map, Vector2d initialPosition, int genomeLength, int startEnergy) {
        this(map, initialPosition, generateRandomGenome(genomeLength), startEnergy, 0);
    }

    public Animal(IWorldMap map, Vector2d initialPosition, List<Integer>genome, int startEnergy, int currentDy) {
        this.map = map;
        this.position = initialPosition;
        this.map.place(this);
        this.genome = genome;
        this.currentGeneIndex = random.nextInt(genome.size());
        this.orientation = MapDirection.values()[random.nextInt(8)];
        this.energy = startEnergy;
        this.birthDay = currentDy;
        this.image = loadRandomImage();
    }

    public void setParents(Animal parent1, Animal parent2) {
        this.parents.add(parent1);
        this.parents.add(parent2);
    }

    private static List<Integer> generateRandomGenome(int genomeLength) {
        List<Integer> result = new ArrayList<>();
        for (int i =0; i<genomeLength; i++) {
            int gene = random.nextInt(0, 8);
            result.add(gene);
        }
        return result;
    }

    public String toString() {
        return orientation.toString();
    }

    public int getAge(){
        return age;
    }

    public void move(SimulationPropertyFile simulationPropertyFile) {
        boolean skipMove = false;
        MapDirection originalOrientation = this.orientation;
        this.orientation = this.orientation.rotate(genome.get(currentGeneIndex));

        if (simulationPropertyFile.getIntValue(ESimulationProperty.wariantZachowaniaZwierzakow) == 1) {
            //pełna predestynacja
            currentGeneIndex = (currentGeneIndex + 1) % this.genome.size();

        } else {
            //starość nie radość
            int age = this.getAge();
            int maxAgeForEffect = 80;
            double skipProbability = Math.min(age, maxAgeForEffect) * 0.01;
            skipMove = (Math.random() < skipProbability);

            if (!skipMove) {
                currentGeneIndex = (currentGeneIndex + 1) % this.genome.size();
            }
        }

        Vector2d newPosition = this.position.add(orientation.toUnitVector());
        if (!skipMove) {
            if (map.canMoveTo(newPosition)) {
                this.position = newPosition;
            } else {
                // kula ziemska
                int y = newPosition.y;
                if ((y == -1) || (y == map.getHeight())) {
                    this.orientation = orientation.rotate(4);
                } else {
                    int x = newPosition.x;
                    if (x == -1) {
                        x = map.getWidth() - 1;
                    } else if (x == map.getWidth()) {
                        x = 0;
                    }
                    this.position = new Vector2d(x, y);
                }
            }
        }
        else{
            this.orientation = originalOrientation;
        }
        this.energy--;
        this.age++;
    }
    public int getBirthDay() {
        return birthDay;
    }
}
