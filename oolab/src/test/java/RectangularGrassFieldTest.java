import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.map.Vector2d;
import project.map.RectangularGrassField;
import project.mapElements.Animal;
import project.gui.SimulationPropertyFile;
import project.gui.ESimulationProperty;
import project.mapElements.Grass;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RectangularGrassFieldTest {
    private RectangularGrassField map;
    private List<Integer> genome;

    @BeforeEach
    void setUp() {
        map = new RectangularGrassField(10, 10, 20);

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

        SimulationPropertyFile simProps = new SimulationPropertyFile(props);
    }

    @Test
    void testPlaceAnimal() {
        Animal newAnimal = new Animal(map, new Vector2d(5, 5), genome, 100, 0);
        assertTrue(map.place(newAnimal));
        assertTrue(map.getAnimals().contains(newAnimal));
    }

    @Test
    void testPlantGrass() {
        map = new RectangularGrassField(10, 10, 0);

        Map<Vector2d, Grass> grassMap = map.getPositionToGrassMap();
        map.plantGrass(5, grassMap, new HashMap<>());
        assertEquals(5, grassMap.size());
    }

    @Test
    void testIsOccupied() {
        Animal animal = new Animal(map, new Vector2d(5, 5), genome, 100, 0);
        map.place(animal);
        assertTrue(map.isOccupied(new Vector2d(5, 5)));
        assertFalse(map.isOccupied(new Vector2d(1, 1)));
    }
}
