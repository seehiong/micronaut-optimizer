package io.github.seehiong.model.output;

import java.util.UUID;

import io.github.seehiong.model.SolverState;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class Output {

    protected UUID solverId; // unique identifier of the solver instance
    protected SolverState solverState; // state of the solver
    protected String message; // generic message
    protected int iteration; // number of iterations
    protected long elapsedTime; // in milliseconds
}
