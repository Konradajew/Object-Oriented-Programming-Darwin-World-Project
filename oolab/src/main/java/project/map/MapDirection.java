package project.map;

public enum MapDirection {

    NORTH(new Vector2d(0, 1), "N"),
    NORTH_EAST(new Vector2d(1, 1), "NE"),
    EAST(new Vector2d(1, 0), "E"),
    SOUTH_EAST(new Vector2d(1, -1), "SE"),
    SOUTH(new Vector2d(0, -1), "S"),
    SOUTH_WEST(new Vector2d(-1, -1), "SW"),
    WEST(new Vector2d(-1, 0), "W"),
    NORTH_WEST(new Vector2d(-1, 1), "NW");

    private final Vector2d unitVector;
    private final String displayName;

    MapDirection(Vector2d unitVector, String displayName) {
        this.unitVector = unitVector;
        this.displayName = displayName;
    }

    public MapDirection rotate(int rotationCount) {
        int newDirectionOrdinal = (this.ordinal() + rotationCount) % MapDirection.values().length;
        MapDirection newMapDirection;
        newMapDirection = MapDirection.values()[newDirectionOrdinal];
        return newMapDirection;
    }

    public Vector2d toUnitVector() {
        return this.unitVector;
    }

    public String toString() {
        return this.displayName;
    }
}
