package io.github.seehiong.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {

    private double x;
    private double y;

}
