package io.github.seehiong.model.constraint;

import java.util.Arrays;

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
public class VehicleConstraint implements Constraint {

    private int vehicleNumber;
    private long capacity;
    private long[] capacities;

    public VehicleConstraint(int vehicleNumber, long capacity) {
        this.vehicleNumber = vehicleNumber;
        this.capacity = capacity;
        this.capacities = new long[vehicleNumber];
        Arrays.fill(this.capacities, capacity);
    }
}
