package io.github.seehiong.model.input;

import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.SolveTimeConstraint;
import io.github.seehiong.model.objective.MinMaxEnum;
import io.github.seehiong.model.objective.MinMaxObjective;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TSPInput extends Input {

    private DistanceMatrixConstraint distanceMatrixConstraint;
    private SolveTimeConstraint solveTimeConstraint;
    private MinMaxObjective minMaxObjective;

    public TSPInput(DistanceMatrixConstraint distanceMatrixConstraint,
            SolveTimeConstraint solveTimeConstraint, MinMaxObjective minMaxObjective) {
        super();
        this.distanceMatrixConstraint = distanceMatrixConstraint;
        this.solveTimeConstraint = solveTimeConstraint;
        this.minMaxObjective = minMaxObjective;
    }

    public double[][] getDistances() {
        if (distanceMatrixConstraint == null) {
            return null;
        }
        return distanceMatrixConstraint.getDistances();
    }

    public String getSolveTime() {
        if (solveTimeConstraint == null) {
            return null;
        }
        return solveTimeConstraint.getSolveTime();
    }

    public MinMaxEnum getMinMaxEnum() {
        if (minMaxObjective == null) {
            return null;
        }
        return minMaxObjective.getMinMaxEnum();
    }
}
