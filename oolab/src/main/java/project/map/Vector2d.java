package project.map;

import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Vector2d {
    final public Integer x;
    final public Integer y;

    public Vector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + this.x +", " + this.y + ")";
    }

    public boolean precedes(Vector2d other) {
        return this.x <= other.x && this.y <= other.y;
    }

    public boolean follows(Vector2d other) {
        return this.x >= other.x && this.y >= other.y;
    }
    public Vector2d upperRight(Vector2d other) {
        Vector2d supremum;
        supremum = new Vector2d(max(this.x, other.x), max(this.y, other.y));
        return supremum;
    }

    public Vector2d lowerLeft(Vector2d other) {
        Vector2d infimum;
        infimum = new Vector2d(min(this.x, other.x), min(this.y, other.y));
        return infimum;
    }

    public Vector2d add(Vector2d other) {
        Vector2d sum;
        sum = new Vector2d(this.x+ other.x, this.y+ other.y);
        return sum;
    }

    public Vector2d subtract(Vector2d other) {
        Vector2d result;
        result = new Vector2d(this.x- other.x, this.y- other.y);
        return result;
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Vector2d another) {
            return this.x.equals(another.x) && this.y.equals(another.y);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
