import project.map.MapDirection;
import project.map.Vector2d;
import project.map.RectangularGrassField;
import project.mapElements.Animal;
import project.gui.SimulationPropertyFile;
import project.gui.ESimulationProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {
    private Animal animal;
    private Vector2d initialPosition;
    private SimulationPropertyFile simProps;
    private List<Integer> genome;
    private RectangularGrassField map;

    @BeforeEach
    void setUp() throws Exception {
        map = new RectangularGrassField(10, 10, 20);
        initialPosition = new Vector2d(2, 3);
        genome = Arrays.asList(0, 1, 2, 3, 4);

        Properties props = new Properties();
        props.setProperty(ESimulationProperty.dlugoscGenomuZwierzakow.toString(), "5");
        props.setProperty(ESimulationProperty.startowaEnergiaZwierzakow.toString(), "100");
        props.setProperty(ESimulationProperty.wariantZachowaniaZwierzakow.toString(), "1");
        props.setProperty(ESimulationProperty.energiaZapewnianaPrzezZjedzenieJednejRosliny.toString(), "20");
        props.setProperty(ESimulationProperty.energiaZwierzakaGotowegoDoRozmnazania.toString(), "30");
        props.setProperty(ESimulationProperty.energiaRodzicowDoTworzeniaPotomka.toString(), "20");
        props.setProperty(ESimulationProperty.minimalnaLiczbaMutacji.toString(), "1");
        props.setProperty(ESimulationProperty.maksymalnaLiczbaMutacji.toString(), "3");
        props.setProperty(ESimulationProperty.animationStepDelay.toString(), "1000");
        props.setProperty(ESimulationProperty.energiaZapewnianiaPrzezZjedzenieWielkiejRosliny.toString(), "10");
        props.setProperty(ESimulationProperty.liczbaRoslinWyrastajacaKazdegoDnia.toString(), "5");
        props.setProperty(ESimulationProperty.wariantWzrostuRoslin.toString(), "0");

        simProps = new SimulationPropertyFile(props);
        animal = new Animal(map, initialPosition, genome, 100, 0);
    }

    @Test
    void testConstructorInitialization() {
        assertEquals(initialPosition, animal.getPosition());
        assertEquals(genome, animal.getGenome());
        assertEquals(100, animal.getEnergy());
        assertEquals(0, animal.getAge());
        assertEquals(0, animal.getPlantsEaten());
        assertEquals(0, animal.getChildrenCount());
        assertTrue(animal.isAlive());
        assertTrue(map.getAnimals().contains(animal));
    }

    @Test
    void testEnergyLossOnMove() {
        int initialEnergy = animal.getEnergy();
        animal.move(simProps);
        assertEquals(initialEnergy - 1, animal.getEnergy());
    }

    @Test
    void testEatingGrass() {
        Properties props = new Properties();
        props.setProperty(ESimulationProperty.energiaZapewnianaPrzezZjedzenieJednejRosliny.toString(), "20"); // Dodaj tę linię
        props.setProperty(ESimulationProperty.dlugoscGenomuZwierzakow.toString(), "5");
        props.setProperty(ESimulationProperty.startowaEnergiaZwierzakow.toString(), "100");
        SimulationPropertyFile localSimProps = new SimulationPropertyFile(props);

        Animal localAnimal = new Animal(map, initialPosition, genome, 100, 0);

        int initialEnergy = localAnimal.getEnergy();
        localAnimal.setEnergy(initialEnergy + localSimProps.getIntValue(ESimulationProperty.energiaZapewnianaPrzezZjedzenieJednejRosliny));
        localAnimal.incrementPlantsEaten();

        assertEquals(initialEnergy + 20, localAnimal.getEnergy());
        assertEquals(1, localAnimal.getPlantsEaten());
    }

    @Test
    void testMovementAndOrientationChange() throws Exception {
        Animal testAnimal = new Animal(map, new Vector2d(5, 5), Arrays.asList(0, 1, 2, 3), 100, 0);

        Field geneIndexField = Animal.class.getDeclaredField("currentGeneIndex");
        geneIndexField.setAccessible(true);
        geneIndexField.set(testAnimal, 0);

        Field orientationField = Animal.class.getDeclaredField("orientation");
        orientationField.setAccessible(true);
        orientationField.set(testAnimal, MapDirection.NORTH);

        testAnimal.move(simProps);

        assertEquals(MapDirection.NORTH, testAnimal.orientation);
        assertEquals(1, testAnimal.currentGeneIndex);
    }

    @Test
    void testAging() {
        int initialAge = animal.getAge();
        animal.move(simProps);
        assertEquals(initialAge + 1, animal.getAge());
    }

    @Test
    void testChildrenTracking() {
        Animal parent = new Animal(map, initialPosition, genome, 100, 0);
        Animal child = new Animal(map, initialPosition, genome, 50, 0);

        child.setParents(parent, parent);
        parent.addChild(child);

        assertEquals(1, parent.getChildren().size());
    }

    @Test
    void testEnergyAfterEating() {
        int initialEnergy = animal.getEnergy();
        animal.setEnergy(initialEnergy + simProps.getIntValue(ESimulationProperty.energiaZapewnianaPrzezZjedzenieJednejRosliny));
        assertEquals(initialEnergy + 20, animal.getEnergy());
    }
}