package io.github.seehiong.model.input;

import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.SolveTimeConstraint;
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

    private DistanceMatrixConstraint distanceMatrixConstraint;  // Distance matrix between locations

    public TSPInput(DistanceMatrixConstraint distanceMatrixConstraint, MinMaxObjective minMaxObjective,
            SolveTimeConstraint solveTimeConstraint) {
        super();
        super.minMaxObjective = minMaxObjective;
        super.solveTimeConstraint = solveTimeConstraint;
        this.distanceMatrixConstraint = distanceMatrixConstraint;
    }

    public double[][] getDistances() {
        if (distanceMatrixConstraint == null) {
            return null;
        }
        return distanceMatrixConstraint.getDistances();
    }

}
