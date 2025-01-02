package io.github.seehiong.model.input;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.constraint.CustomerCoordinateConstraint;
import io.github.seehiong.model.constraint.CustomerDemandConstraint;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.SolveTimeConstraint;
import io.github.seehiong.model.constraint.VehicleConstraint;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CVRPInput extends Input {

    private VehicleConstraint vehicleConstraint;
    private CustomerDemandConstraint customerDemandConstraint;
    private CustomerCoordinateConstraint customerCoordinateConstraint;
    private DistanceMatrixConstraint distanceMatrixConstraint;
    private SolveTimeConstraint solveTimeConstraint;

    public CVRPInput(VehicleConstraint vehicleConstraint,
            CustomerDemandConstraint customerDemandConstraint, CustomerCoordinateConstraint customerCoordinateConstraint,
            DistanceMatrixConstraint distanceMatrixConstraint, SolveTimeConstraint solveTimeConstraint) {
        super();
        this.vehicleConstraint = vehicleConstraint;
        this.customerDemandConstraint = customerDemandConstraint;
        this.customerCoordinateConstraint = customerCoordinateConstraint;
        this.distanceMatrixConstraint = distanceMatrixConstraint;
        this.solveTimeConstraint = solveTimeConstraint;
    }

    public int getVehicleNumber() {
        if (vehicleConstraint == null) {
            return 0;
        }
        return vehicleConstraint.getVehicleNumber();
    }

    public long[] getCapacities() {
        if (vehicleConstraint == null) {
            return null;
        }
        return vehicleConstraint.getCapacities();
    }

    public int[] getDemands() {
        if (customerDemandConstraint == null) {
            return null;
        }
        return customerDemandConstraint.getDemands();
    }

    public Coordinate[] getCoordinates() {
        if (customerCoordinateConstraint == null) {
            return null;
        }
        return customerCoordinateConstraint.getCoordinates();
    }

    public double[][] getDistances() {
        if (distanceMatrixConstraint == null) {
            return null;
        }
        return distanceMatrixConstraint.getDistances();
    }

    public long getTimeInSeconds() {
        if (solveTimeConstraint == null) {
            return SolveTimeConstraint.DEFAULT_TIME_IN_SECONDS;
        }
        return solveTimeConstraint.getTimeInSeconds();
    }
}