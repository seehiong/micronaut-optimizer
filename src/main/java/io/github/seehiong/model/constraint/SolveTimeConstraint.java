package io.github.seehiong.model.constraint;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolveTimeConstraint implements Constraint {

    public static final long DEFAULT_TIME_IN_SECONDS = 60; // 1 minute

    private String solveTime; // e.g. "1s", "1m", "1h"
    private long timeInSeconds; // e.g. 1, 60, 3600
}
