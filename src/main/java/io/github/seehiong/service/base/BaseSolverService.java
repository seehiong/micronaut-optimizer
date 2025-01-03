package io.github.seehiong.service.base;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.input.Input;
import io.github.seehiong.model.output.Output;

public abstract class BaseSolverService<I extends Input, O extends Output> implements SolverService<I, O> {

    protected double calculateDistance(Coordinate from, Coordinate to) {
        double dx = from.getX() - to.getX();
        double dy = from.getY() - to.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected double calculateDistance(double x1, double y1, double x2, double y2) {
        double xDelta = (x1 - x2);
        double yDelta = (y1 - y2);
        return Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
    }
}
