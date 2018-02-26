package com.merteroglu.trajectory.Model;

import java.util.ArrayList;

/**
 * Created by Mert on 26.02.2018.
 */

public class SearchingBody {
    ArrayList<Coordinate> allCoordinates;
    Coordinate topLeft;
    Coordinate bottomRight;

    public ArrayList<Coordinate> getAllCoordinates() {
        return allCoordinates;
    }

    public void setAllCoordinates(ArrayList<Coordinate> allCoordinates) {
        this.allCoordinates = allCoordinates;
    }


    public Coordinate getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Coordinate topLeft) {
        this.topLeft = topLeft;
    }

    public Coordinate getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Coordinate bottomRight) {
        this.bottomRight = bottomRight;
    }
}
