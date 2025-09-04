import org.junit.jupiter.api.Test;
import project.map.MapDirection;
import project.map.Vector2d;

import static org.junit.jupiter.api.Assertions.*;

class MapDirectionTest {

    @Test
    void testRotate() {
        assertEquals(MapDirection.NORTH.rotate(1), MapDirection.NORTH_EAST, "NORTH should rotate to NORTH_EAST");
        assertEquals(MapDirection.NORTH.rotate(2), MapDirection.EAST, "NORTH should rotate to EAST");
        assertEquals(MapDirection.NORTH.rotate(3), MapDirection.SOUTH_EAST, "NORTH should rotate to SOUTH_EAST");
        assertEquals(MapDirection.NORTH.rotate(4), MapDirection.SOUTH, "NORTH should rotate to SOUTH");
        assertEquals(MapDirection.NORTH.rotate(5), MapDirection.SOUTH_WEST, "NORTH should rotate to SOUTH_WEST");
        assertEquals(MapDirection.NORTH.rotate(6), MapDirection.WEST, "NORTH should rotate to WEST");
        assertEquals(MapDirection.NORTH.rotate(7), MapDirection.NORTH_WEST, "NORTH should rotate to NORTH_WEST");
        assertEquals(MapDirection.NORTH.rotate(8), MapDirection.NORTH, "NORTH should rotate back to NORTH");
    }

    @Test
    void testToUnitVector() {
        assertEquals(MapDirection.NORTH.toUnitVector(), new Vector2d(0, 1), "NORTH should convert to (0, 1)");
        assertEquals(MapDirection.NORTH_EAST.toUnitVector(), new Vector2d(1, 1), "NORTH_EAST should convert to (1, 1)");
        assertEquals(MapDirection.EAST.toUnitVector(), new Vector2d(1, 0), "EAST should convert to (1, 0)");
        assertEquals(MapDirection.SOUTH_EAST.toUnitVector(), new Vector2d(1, -1), "SOUTH_EAST should convert to (1, -1)");
        assertEquals(MapDirection.SOUTH.toUnitVector(), new Vector2d(0, -1), "SOUTH should convert to (0, -1)");
        assertEquals(MapDirection.SOUTH_WEST.toUnitVector(), new Vector2d(-1, -1), "SOUTH_WEST should convert to (-1, -1)");
        assertEquals(MapDirection.WEST.toUnitVector(), new Vector2d(-1, 0), "WEST should convert to (-1, 0)");
        assertEquals(MapDirection.NORTH_WEST.toUnitVector(), new Vector2d(-1, 1), "NORTH_WEST should convert to (-1, 1)");
    }

    @Test
    void testToString() {
        assertEquals(MapDirection.NORTH.toString(), "N", "NORTH should display as 'N'");
        assertEquals(MapDirection.NORTH_EAST.toString(), "NE", "NORTH_EAST should display as 'NE'");
        assertEquals(MapDirection.EAST.toString(), "E", "EAST should display as 'E'");
        assertEquals(MapDirection.SOUTH_EAST.toString(), "SE", "SOUTH_EAST should display as 'SE'");
        assertEquals(MapDirection.SOUTH.toString(), "S", "SOUTH should display as 'S'");
        assertEquals(MapDirection.SOUTH_WEST.toString(), "SW", "SOUTH_WEST should display as 'SW'");
        assertEquals(MapDirection.WEST.toString(), "W", "WEST should display as 'W'");
        assertEquals(MapDirection.NORTH_WEST.toString(), "NW", "NORTH_WEST should display as 'NW'");
    }
}