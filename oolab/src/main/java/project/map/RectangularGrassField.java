package project.map;

import project.mapElements.Animal;
import project.mapElements.BigGrass;
import project.mapElements.Grass;
import project.mapElements.IMapElement;

import java.util.*;
import java.util.stream.Collectors;

public class RectangularGrassField extends AbstractWorldMap {
    private static Random random = new Random();

    private static final Vector2d LOWER_LEFT = new Vector2d(0, 0);
    private final Vector2d upperRight;

    public RectangularGrassField(int width, int height, int grassCount) {

        upperRight = new Vector2d(width-1, height-1);

        Map<Vector2d, Grass> grassMap = new HashMap<>();
        Map<Vector2d, BigGrass> bigGrassMap = new HashMap<>();
        plantGrass(grassCount, grassMap, bigGrassMap);

    }

    // Sadzenie trawy na mapie
    public void plantGrass(int grassCount, Map<Vector2d, Grass> grassMap, Map<Vector2d, BigGrass> bigGrassMap) {
        int fieldSize = getWidth() * getHeight();
        int counter = 0;
        while (counter < grassCount && grassMap.size() < fieldSize) {
            Vector2d position = generateElementPosition(grassMap, bigGrassMap);
            Grass grass = new Grass(position);
            this.mapElements.add(grass);
            grassMap.put(position, grass);
            counter++;
        }
    }

    // Pobranie mapy pozycji do trawy
    public Map<Vector2d, Grass> getPositionToGrassMap() {
        Map<Vector2d, Grass> grassMap = new HashMap<>();
        for (Grass grass : getGrassList()) {
            grassMap.put(grass.getPosition(), grass);
        }
        return grassMap;
    }

    // Pobranie mapy pozycji do dużej trawy
    public Map<Vector2d, BigGrass> getPositionToBigGrassMap() {
        Map<Vector2d, BigGrass> grassMap = new HashMap<>();
        for (BigGrass grass : getBigGrassList()) {
            for (Vector2d position : grass.getCoveredPositions()) {
                grassMap.put(position, grass);
            }
        }
        return grassMap;
    }

    public Vector2d generateRandomPosition() {
        int x = random.nextInt(0, getWidth());
        int y = random.nextInt(0, getHeight());

        Vector2d position;
        position = new Vector2d(x, y);
        return position;
    }

    public Vector2d generateRandomGrassPosition() {
        int x = random.nextInt(0, getWidth());
        boolean preferEquator = random.nextDouble() < 0.8;
        if (preferEquator) {
            int equatorHeight = getHeight() / 5;
            int equatorStart = (getHeight() - equatorHeight) / 2;
            int y = random.nextInt(equatorHeight) + equatorStart;
            return new Vector2d(x, y);
        } else {
            int y = random.nextInt(0, getHeight());
            return new Vector2d(x, y);
        }
    }

    public int getHeight() {
        return upperRight.y + 1;
    }

    public int getWidth() {
        return upperRight.x + 1;
    }

    public Vector2d generateElementPosition(Map<Vector2d, Grass> grassMap, Map<Vector2d, BigGrass> bigGrassMap) {
        Vector2d newPosition = generateRandomGrassPosition();
        while (grassMap.containsKey(newPosition)) {
            newPosition = generateRandomGrassPosition();
        }
        return newPosition;
    }

    @Override
    public boolean isOnMap(Vector2d position) {
        return position.follows(LOWER_LEFT) && position.precedes(upperRight);
    }

    public int getEmptyFieldCount() {
        int allFields = this.getWidth() * this.getHeight();
        int occupiedFields = mapElements.stream().map(IMapElement::getPosition).collect(Collectors.toSet()).size();
        return allFields-occupiedFields;
    }

    public Map.Entry<String, Integer>  getMostPopularGenes() {
        Map<String, Integer> genomeOccurences = new HashMap<>();
        for (Animal a : getAnimals()) {
            String key = a.getGenome().toString();
            genomeOccurences.computeIfPresent(key, (k, v)-> v+1);
            genomeOccurences.computeIfAbsent(key, k-> 1);
        }
        Map.Entry<String, Integer> result;
        result = genomeOccurences.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).orElse(null);
        return result;
    }

    public void plantOnlyNormalGrass(int count, Map<Vector2d, Grass> grassMap) {
        List<Vector2d> freePositions = RandomPositionGenerator.getNormalPositions(grassMap, getWidth(), getHeight(), count);
        for (Vector2d position : freePositions) {
            Grass grass = new Grass(position);
            grassMap.put(position, grass);
            this.mapElements.add(grass);
        }
    }

    public void plantNormalAndBigGrass(int grassCount, Map<Vector2d, Grass> grassMap, Map<Vector2d, BigGrass> bigGrassMap) {
        int bigGrassCount = grassCount / 5; // 20% trawy (1/5)
        int normalGrassCount = grassCount - bigGrassCount; // reszta (80%)
        int subareaSize = (getHeight()+getWidth()) / 4;
        Map<Vector2d, Integer> freePositions = RandomPositionGenerator.generateGrassPositions(grassMap, bigGrassMap, getWidth(), subareaSize,
                2, normalGrassCount, bigGrassCount, getHeight());
        for (Vector2d position : freePositions.keySet()) {
            int grassType = freePositions.get(position);
            // 1 - bigGrass, 0 - normalGrass
            if (grassType == 1) {
                BigGrass bigGrass = new BigGrass(position);
                for (Vector2d pos : bigGrass.getCoveredPositions()) {
                    bigGrassMap.put(pos, bigGrass);
                }
                this.mapElements.add(bigGrass);
            } else {
                Grass grass = new Grass(position);
                grassMap.put(position, grass);
                this.mapElements.add(grass);
            }
        }
    }

    public List<Vector2d> getPreferredEmptyFields() {
        int height = getHeight();
        int width = getWidth();
        int equatorHeight = height / 5;
        int equatorStart = (height - equatorHeight) / 2;

        List<Vector2d> emptyPositions = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = equatorStart; y < equatorStart + equatorHeight; y++) {
                Vector2d pos = new Vector2d(x, y);
                boolean isOccupied = mapElements.stream().anyMatch(element -> {
                    if (element instanceof BigGrass) {
                        Vector2d lowerLeft = element.getPosition();
                        for (int dx = 0; dx < 2; dx++) {
                            for (int dy = 0; dy < 2; dy++) {
                                if (lowerLeft.add(new Vector2d(dx, dy)).equals(pos)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    } else {
                        return element.getPosition().equals(pos);
                    }
                });

                if (!isOccupied) {
                    emptyPositions.add(pos);
                }

            }
        }

        return emptyPositions;
    }

    public List<Vector2d> getPreferredBigGrassEmptyFields() {
        int subareaSize = (getHeight()+getWidth()) / 4;
        int subareaStartX = getWidth() - subareaSize;
        int subareaStartY = 0;

        List<Vector2d> emptyPositions = new ArrayList<>();

        for (int x = subareaStartX; x < getWidth(); x++) {
            for (int y = 0; y < subareaStartY + subareaSize - getHeight() / 10; y++) {
                Vector2d position = new Vector2d(x, y);

                boolean isOccupied = mapElements.stream().anyMatch(element -> {
                    if (element instanceof BigGrass) {
                        Vector2d lowerLeft = element.getPosition();
                        for (int dx = 0; dx < 2; dx++) {
                            for (int dy = 0; dy < 2; dy++) {
                                if (lowerLeft.add(new Vector2d(dx, dy)).equals(position)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    } else {
                        return element.getPosition().equals(position);
                    }
                });

                if (!isOccupied) {
                    emptyPositions.add(position);
                }
            }
        }
        return emptyPositions;
    }

}
