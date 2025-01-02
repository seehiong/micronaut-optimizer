package io.github.seehiong.model.input;

import java.util.UUID;

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
}
