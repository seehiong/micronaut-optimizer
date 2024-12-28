package io.github.seehiong.model.input;

import java.util.UUID;

import io.github.seehiong.model.ProblemType;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@SuperBuilder
@AllArgsConstructor
public abstract class Input {

    protected UUID solverId;
    protected ProblemType problemType;

    public Input() {
        this.solverId = UUID.randomUUID();
    }

    public Input(ProblemType problemType) {
        this();
        this.problemType = problemType;
    }

}
