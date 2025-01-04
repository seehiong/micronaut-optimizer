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

    private int vehicleNumber; // number of vehicles
    private long capacity;  // same capacity for every vehicle
    private long[] capacities; // capacity of each vehicle

    /**
     * For single capacity
     *
     * @param vehicleNumber
     * @param capacity
     */
    public VehicleConstraint(int vehicleNumber, long capacity) {
        this.vehicleNumber = vehicleNumber;
        this.capacity = capacity;
        this.capacities = new long[vehicleNumber];
        Arrays.fill(this.capacities, capacity);
    }

    /**
     * For multiple capacities
     *
     * @param vehicleNumber
     * @param capacities
     */
    public VehicleConstraint(int vehicleNumber, long[] capacities) {
        this.vehicleNumber = vehicleNumber;
        this.capacities = capacities;
        this.capacity = 0; // not used
    }
}
