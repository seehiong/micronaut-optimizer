package io.github.seehiong.model.objective;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
public enum MinMaxEnum {
    MINIMIZE,
    MAXIMIZE;
}
