package project.map;

import project.mapElements.BigGrass;
import project.mapElements.Grass;

import java.util.*;
import java.util.stream.Collectors;

public class RandomPositionGenerator {
    private static Random rand = new Random();

    // zalesione równiki
    public static List<Vector2d> getNormalPositions(Map<Vector2d, Grass> grassMap, int width, int height, int positionsToGenerate) {
        // Pobierz wszystkie wolne pozycje
        List<Vector2d> freePositions = getFreePositions(grassMap, height, width);

        // Jeśli liczba wolnych pozycji jest mniejsza niż wymagana
        if (freePositions.size() < positionsToGenerate) {
            Collections.shuffle(freePositions, rand);
            return freePositions;
        }

        // Preferuj pozycje na równiku
        List<Vector2d> finalPositions = preferEquatorPositions(freePositions, positionsToGenerate, height);

        return finalPositions;
    }

    public static List<Vector2d> getAbnormalPositions(Map<Vector2d, Grass> grassMap, Map<Vector2d, BigGrass> bigGrassMap, int width, int height) {

        List<Vector2d> freePositions = getFreePositions(grassMap, height, width);

        return freePositions.stream().filter(x -> !bigGrassMap.containsKey(x)).collect(Collectors.toList());
    }

    private static List<Vector2d> getFreePositions(Map<Vector2d, Grass> grassMap, int height, int width) {
        List<Vector2d> allPositions = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                allPositions.add(new Vector2d(x, y));
            }
        }

        // Filtruj zajęte pozycje
        return allPositions.stream()
                .filter(pos -> !grassMap.containsKey(pos))
                .collect(Collectors.toList());
    }

    private static List<Vector2d> preferEquatorPositions(List<Vector2d> freePositions, int positionsToGenerate, int height) {
        int equatorHeight = height / 5; // Wysokość pasa równika
        int equatorStart = (height - equatorHeight) / 2; // Początek pasa równika

        // Pozycje równikowe
        List<Vector2d> equatorPositions = freePositions.stream()
                .filter(pos -> pos.y >= equatorStart && pos.y < equatorStart + equatorHeight)
                .collect(Collectors.toList());

        // Mieszaj pozycje na równiku i poza nim
        Collections.shuffle(equatorPositions, rand);
        List<Vector2d> nonEquatorPositions = new ArrayList<>(freePositions);
        nonEquatorPositions.removeAll(equatorPositions);
        Collections.shuffle(nonEquatorPositions, rand);

        // Preferuj pozycje na równiku
        List<Vector2d> finalPositions = new ArrayList<>();
        int equatorQuota = Math.min((int) (positionsToGenerate * 0.8), equatorPositions.size());

        finalPositions.addAll(equatorPositions.stream().limit(equatorQuota).collect(Collectors.toList()));
        finalPositions.addAll(nonEquatorPositions.stream().limit(positionsToGenerate - equatorQuota).collect(Collectors.toList()));

        return finalPositions;
    }

    // Zalesione równiki i dorodne plony
    public static Map<Vector2d, Integer> generateGrassPositions(
            Map<Vector2d, Grass> grassMap,
            Map<Vector2d, BigGrass> bigGrassMap,
            int fieldSize,
            int subareaSize,
            int squareSize,
            int normalGrassCount,
            int bigGrassCount,
            int height
    ) {
        Map<Vector2d, Integer> result = new HashMap<>();

        // Znajdź wolne kwadraty dla dużej trawy (2x2)
        List<Vector2d> freeSquares = getFreeSquares(grassMap, bigGrassMap, fieldSize, height, squareSize);

        // Preferuj duże trawy w podobszarze
        List<Vector2d> bigGrassPositions = preferSubareaForSquares(freeSquares, fieldSize, subareaSize, bigGrassCount);
        Set<Vector2d> occupiedPositions = new HashSet<>();
        // Dodaj duże trawy do wyniku
        for (Vector2d position : bigGrassPositions) {
            for (int dx = 0; dx < squareSize; dx++) {
                for (int dy = 0; dy < squareSize; dy++) {
                    Vector2d pos = position.add(new Vector2d(dx, dy));
                    occupiedPositions.add(pos);
                }
            }
            result.put(position, 1); // Typ 1 = BigGrass
        }

        // Znajdź wolne pozycje dla normalnej trawy
        List<Vector2d> freePositions = getFreePositions(grassMap, bigGrassMap, fieldSize, height).stream()
                .filter(pos -> !occupiedPositions.contains(pos)) // Usuń zajęte przez BigGrass
                .collect(Collectors.toList());

        // Preferuj normalną trawę na równiku
        List<Vector2d> normalGrassPositions = preferEquatorPositions(freePositions, normalGrassCount, height);

        // Dodaj normalną trawę do wyniku
        for (Vector2d position : normalGrassPositions) {
            result.put(position, 0); // Typ 0 = NormalGrass
        }

        return result;
    }

    public static List<Vector2d> getFreeSquares(Map<Vector2d, Grass> grassMap, Map<Vector2d, BigGrass> bigGrassMap, int fieldSize, int height, int squareSize) {
        List<Vector2d> freeSquares = new ArrayList<>();
        for (int x = 0; x < fieldSize - squareSize + 1; x++) {
            for (int y = 0; y < height - squareSize + 1; y++) {
                Vector2d lowerLeft = new Vector2d(x, y);

                boolean isSquareFree = true;
                for (int dx = 0; dx < squareSize; dx++) {
                    for (int dy = 0; dy < squareSize; dy++) {
                        Vector2d position = lowerLeft.add(new Vector2d(dx, dy));
                        if (grassMap.containsKey(position) || bigGrassMap.containsKey(position)) {
                            isSquareFree = false;
                            break;
                        }
                    }
                    if (!isSquareFree) break;
                }

                if (isSquareFree) {
                    freeSquares.add(lowerLeft);
                }
            }
        }
        return freeSquares;
    }

    public static List<Vector2d> preferSubareaForSquares(
            List<Vector2d> freeSquares,
            int fieldSize,
            int subareaSize,
            int positionsToGenerate
    ) {
        // Wyliczanie granic podobszaru w prawym dolnym rogu
        int subareaStartX = fieldSize - subareaSize;
        int subareaStartY = 0;

        // Wybieramy kwadraty w obrębie podobszaru
        List<Vector2d> subareaSquares = freeSquares.stream()
                .filter(pos -> pos.x >= subareaStartX && pos.y >= subareaStartY && pos.y < subareaStartY + subareaSize)
                .collect(Collectors.toList());

        // Mieszamy preferowane i inne kwadraty
        Collections.shuffle(subareaSquares);
        List<Vector2d> nonSubareaSquares = new ArrayList<>(freeSquares);
        nonSubareaSquares.removeAll(subareaSquares);
        Collections.shuffle(nonSubareaSquares);

        // Dodajemy 80% z preferowanego obszaru i 20% spoza
        int subareaQuota=0;
        for (int i = 0; i < positionsToGenerate; i++) {
            int randomNumber = rand.nextInt(100) + 1; // Losuje liczbę od 1 do 100
            if (randomNumber < 81) {
                subareaQuota++;
            }
        }
        List<Vector2d> result = new ArrayList<>();
        result.addAll(subareaSquares.stream().limit(subareaQuota).collect(Collectors.toList()));
        result.addAll(nonSubareaSquares.stream().limit(positionsToGenerate - subareaQuota).collect(Collectors.toList()));

        return result;
    }

    private static List<Vector2d> getFreePositions(Map<Vector2d, Grass> grassMap, Map<Vector2d, BigGrass> bigGrassMap, int fieldSize, int height) {
        List<Vector2d> allPositions = new ArrayList<>();
        for (int x = 0; x < fieldSize; x++) {
            for (int y = 0; y < height; y++) {
                allPositions.add(new Vector2d(x, y));
            }
        }

        // Filtruj zajęte pozycje
        return allPositions.stream()
                .filter(pos -> !grassMap.containsKey(pos) && !bigGrassMap.containsKey(pos))
                .collect(Collectors.toList());
    }
}
