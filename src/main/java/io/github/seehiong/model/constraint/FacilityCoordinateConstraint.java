package io.github.seehiong.model.constraint;

import io.github.seehiong.model.Coordinate;
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
public class FacilityCoordinateConstraint implements Constraint {

    private Coordinate[] coordinates; // coordinates of each facility
}
