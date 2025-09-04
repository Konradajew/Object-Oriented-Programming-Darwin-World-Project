package project.mapElements;

import project.map.Vector2d;

public interface IMapElement {

    public Vector2d getPosition();

    public boolean isAt(Vector2d position);

    public String toString();
}
