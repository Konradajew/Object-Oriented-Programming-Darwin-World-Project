package project.map;

import project.mapElements.Animal;

public interface IWorldMap {

    boolean canMoveTo(Vector2d position);

    boolean place(Animal animal);

    boolean isOccupied(Vector2d position);

    Vector2d generateRandomPosition();

    int getHeight();
    int getWidth();
}

