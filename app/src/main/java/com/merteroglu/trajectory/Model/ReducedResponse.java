package com.merteroglu.trajectory.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mert on 26.02.2018.
 */

public class ReducedResponse {
    ArrayList<Coordinate> reducedCoordinates;
    double reducedRate;
    long responseTime;

    public ReducedResponse() {
    }

    public ReducedResponse(ArrayList<Coordinate> reducedCoordinates, double reducedRate, long responseTime) {
        this.reducedCoordinates = reducedCoordinates;
        this.reducedRate = reducedRate;
        this.responseTime = responseTime;
    }

    public ArrayList<Coordinate> getReducedCoordinates() {
        return reducedCoordinates;
    }

    public void setReducedCoordinates(ArrayList<Coordinate> reducedCoordinates) {
        this.reducedCoordinates = reducedCoordinates;
    }

    public double getReducedRate() {
        return reducedRate;
    }

    public void setReducedRate(double reducedRate) {
        this.reducedRate = reducedRate;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReducedResponse that = (ReducedResponse) o;

        if (Double.compare(that.reducedRate, reducedRate) != 0) return false;
        if (Double.compare(that.responseTime, responseTime) != 0) return false;
        return reducedCoordinates != null ? reducedCoordinates.equals(that.reducedCoordinates) : that.reducedCoordinates == null;
    }


    @Override
    public String toString() {
        String coordinates = "";
        for (Coordinate c : reducedCoordinates) {
            coordinates += c.toString() + "\n";
        }

        return "ReducedResponse{" +
                ", reducedRate=" + reducedRate +
                ", responseTime=" + responseTime +
                "reducedCoordinates=" + coordinates +
                '}';
    }
}
