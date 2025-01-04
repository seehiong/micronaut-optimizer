package io.github.seehiong.model.input;

import java.util.UUID;

import io.github.seehiong.model.constraint.SolveTimeConstraint;
import io.github.seehiong.model.objective.MinMaxEnum;
import io.github.seehiong.model.objective.MinMaxObjective;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder; // Required for @Builder.Default
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class Input {

    @Builder.Default
    private UUID solverId = UUID.randomUUID();

    protected MinMaxObjective minMaxObjective;
    protected SolveTimeConstraint solveTimeConstraint;

    public MinMaxEnum getMinMaxEnum() {
        if (minMaxObjective == null) {
            return null;
        }
        return minMaxObjective.getMinMaxEnum();
    }

    public String getSolveTime() {
        if (solveTimeConstraint == null) {
            return null;
        }
        return solveTimeConstraint.getSolveTime();
    }

    public long getTimeInSeconds() {
        if (solveTimeConstraint == null) {
            return 0;
        }
        return solveTimeConstraint.getTimeInSeconds();
    }
}
