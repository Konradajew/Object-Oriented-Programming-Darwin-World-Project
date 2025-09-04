import project.map.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Vector2dTest {

    private Vector2d v00;
    private Vector2d v10;
    private Vector2d v01;
    private Vector2d vzero;
    private Vector2d v11;
    private Vector2d v02;

    @BeforeEach
    void setUp() {
        v00 = new Vector2d(0, 0);
        v10 = new Vector2d(1, 0);
        v01 = new Vector2d(0, 1);
        vzero = new Vector2d(0, 0);
        v11 = new Vector2d(1, 1);
        v02 = new Vector2d(0, 2);
    }

    @Test
    void testEquals() {
        assertEquals(v00, v00);

        assertEquals(v00, vzero);
        assertEquals(vzero, v00);

        assertNotEquals(v01, v10);
        assertNotEquals(v01, v02);

        assertNotEquals(v00, "text");
        assertNotEquals(v00, 1);
        assertNotEquals(v00, 1.0);
        assertNotEquals(v00, 0);
        assertNotEquals(v00, 33000);
        assertNotEquals(v00, true);
        assertNotEquals(v00, false);
    }

    @Test
    void testHashCode() {
        assertEquals(v00.hashCode(), vzero.hashCode());
        assertNotEquals(v01.hashCode(), v10.hashCode());
    }

    @Test
    void testToString() {
        assertEquals("(0, 0)", v00.toString());
        assertEquals("(0, 0)", vzero.toString());
        assertEquals("(1, 0)", v10.toString());
        assertEquals("(0, 1)", v01.toString());
        assertEquals("(0, 2)", v02.toString());

        assertNotEquals("(0,0)", v00.toString());
        assertNotEquals("(0 , 0)", v00.toString());
        assertNotEquals(vzero.toString(), v01.toString());
    }

    @Test
    void testPrecedes() {
        assertTrue(v00.precedes(v01));
        assertTrue(v00.precedes(v10));
        assertTrue(v00.precedes(v00));
        assertTrue(v00.precedes(vzero));
        assertTrue(v01.precedes(v02));

        assertFalse(v11.precedes(v01));
        assertFalse(v02.precedes(v10));
    }

    @Test
    void testFollows() {
        assertFalse(v00.follows(v01));
        assertFalse(v00.follows(v10));
        assertTrue(v00.follows(v00));
        assertTrue(v00.follows(vzero));
        assertFalse(v01.follows(v02));
        assertTrue(v11.follows(v01));
        assertFalse(v02.follows(v10));
    }

    @Test
    void testUpperRight() {
        assertEquals(v01, v00.upperRight(v01));
        assertEquals(new Vector2d(1, 2), v11.upperRight(v02));
        assertEquals(v11, v10.upperRight(v01));
        assertEquals(v10, vzero.upperRight(v10));
    }

    @Test
    void testLowerLeft() {
        assertEquals(v00, v10.lowerLeft(v01));
        assertEquals(v01, v11.lowerLeft(v02));
        assertEquals(v11, v11.lowerLeft(v11));
        assertEquals(v00, vzero.lowerLeft(v10));
    }

    @Test
    void testAdd() {
        assertEquals(v01, v00.add(v01));
        assertEquals(v11, v10.add(v01));
        assertEquals(new Vector2d(1, 3), v02.add(v11));
        assertEquals(v00, v00.add(vzero));
    }

    @Test
    void testSubtract() {
        assertEquals(v01, v01.subtract(v00));
        assertEquals(new Vector2d(-1, -1), v00.subtract(v11));
        assertEquals(new Vector2d(-1, 1), v02.subtract(v11));
        assertEquals(v10, v11.subtract(v01));
    }
}