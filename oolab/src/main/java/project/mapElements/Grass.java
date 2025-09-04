package project.mapElements;

import project.map.Vector2d;

public class Grass extends AbstractMapElement {

    public Grass(Vector2d position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "*";
    }

    public void setPosition(Vector2d newGrassPosition) {
        this.position = newGrassPosition;
    }
}