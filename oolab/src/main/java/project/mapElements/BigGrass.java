package project.mapElements;

import project.map.Vector2d;
import java.util.ArrayList;
import java.util.List;

public class BigGrass implements IMapElement{
    private final Vector2d lowerLeft;
    private final List<Vector2d> coveredPositions = new ArrayList<>();

    public BigGrass(Vector2d lowerLeft) {
        this.lowerLeft = lowerLeft;
        generateCoveredPositions();
    }

    // Generowanie pozycji zajmowanych przez dużą trawę
    private void generateCoveredPositions() {
        for (int x = lowerLeft.x; x < lowerLeft.x + 2; x++) {
            for (int y = lowerLeft.y; y < lowerLeft.y + 2; y++) {
                coveredPositions.add(new Vector2d(x, y));
            }
        }
    }

    public Vector2d getLowerLeft() {
        return lowerLeft;
    }

    public List<Vector2d> getCoveredPositions() {
        return coveredPositions;
    }

    @Override
    public Vector2d getPosition() {
        return lowerLeft;
    }

    @Override
    public boolean isAt(Vector2d position) {
        return this.lowerLeft.equals(position);
    }
}
