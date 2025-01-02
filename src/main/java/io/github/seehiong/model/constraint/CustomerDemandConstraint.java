package io.github.seehiong.model.constraint;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDemandConstraint implements Constraint {

    private int[] demands;

}
