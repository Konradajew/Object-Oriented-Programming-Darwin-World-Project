package project.map;

import project.mapElements.Animal;
import project.mapElements.BigGrass;
import project.mapElements.Grass;
import project.mapElements.IMapElement;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractWorldMap implements IWorldMap {

    protected List<IMapElement> mapElements = new ArrayList<>();

    // Sprawdzenie, czy zwierzę może się przemieścić na daną pozycję
    @Override
    public boolean canMoveTo(Vector2d position) {
        if (position == null)
            return false;
        return isOnMap(position);
    }

    abstract protected boolean isOnMap(Vector2d position);

    // Umieszczenie zwierzęcia na mapie
    @Override
    public boolean place(Animal animal) throws IllegalArgumentException {
        Vector2d position = animal.getPosition();
        if (canMoveTo(position)) {
            mapElements.add(animal);
            return true;
        }
        throw new IllegalArgumentException(animal + " can't be placed on top of another animal");
    }

    // Sprawdzenie, czy dana pozycja jest zajęta
    @Override
    public boolean isOccupied(Vector2d position) {
        return mapElements.stream().anyMatch(e -> e.isAt(position));
    }

    public List<IMapElement> getMapElements() {
        return mapElements;
    }

    // Pobranie listy zwierząt na mapie
    public List<Animal> getAnimals() {
        return mapElements.stream()
                .filter(o -> o instanceof Animal)
                .map(o -> (Animal) o)
                .collect(Collectors.toList());
    }

    // Pobranie listy zwierząt na mapie
    public List<Grass> getGrassList() {
        return mapElements.stream()
                .filter(o -> o instanceof Grass)
                .map(o -> (Grass) o)
                .collect(Collectors.toList());
    }

    // Pobranie listy dużej trawy na mapie
    public List<BigGrass> getBigGrassList() {
        return mapElements.stream()
                .filter(o -> o instanceof BigGrass)
                .map(o -> (BigGrass) o)
                .collect(Collectors.toList());
    }
}
