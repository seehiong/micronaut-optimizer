package io.github.seehiong.model.input;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.ProblemType;
import io.github.seehiong.model.constraint.CustomerDemandConstraint;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.FacilityCapacityConstraint;
import io.github.seehiong.model.constraint.FacilityCostConstraint;
import io.github.seehiong.model.constraint.LocationConstraint;
import io.github.seehiong.model.objective.MinMaxEnum;
import io.github.seehiong.model.objective.MinMaxObjective;
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
public class FLPInput extends Input {

    private FacilityCostConstraint facilityCostConstraint;
    private FacilityCapacityConstraint facilityCapacityConstraint;
    private CustomerDemandConstraint customerDemandConstraint;
    private LocationConstraint locationConstraint;
    private DistanceMatrixConstraint distanceMatrixConstraint;
    private MinMaxObjective minMaxObjective;

    public FLPInput(ProblemType problemType, FacilityCostConstraint facilityCostConstraint,
            FacilityCapacityConstraint facilityCapacityConstraint, CustomerDemandConstraint customerDemandConstraint,
            LocationConstraint locationConstraint, DistanceMatrixConstraint distanceMatrixConstraint,
            MinMaxObjective minMaxObjective) {
        super(problemType);
        this.facilityCostConstraint = facilityCostConstraint;
        this.facilityCapacityConstraint = facilityCapacityConstraint;
        this.customerDemandConstraint = customerDemandConstraint;
        this.locationConstraint = locationConstraint;
        this.distanceMatrixConstraint = distanceMatrixConstraint;
        this.minMaxObjective = minMaxObjective;
    }

    public double[] getSetupCost() {
        if (facilityCostConstraint == null) {
            return null;
        }
        return facilityCostConstraint.getSetupCost();
    }

    public int[] getCapacity() {
        if (facilityCapacityConstraint == null) {
            return null;
        }
        return facilityCapacityConstraint.getCapacity();
    }

    public int[] getDemand() {
        if (customerDemandConstraint == null) {
            return null;
        }
        return customerDemandConstraint.getDemand();
    }

    public Coordinate[] getFacilityCoordinates() {
        if (locationConstraint == null) {
            return null;
        }
        return locationConstraint.getFacilityCoordinates();
    }

    public Coordinate[] getCustomerCoordinates() {
        if (locationConstraint == null) {
            return null;
        }
        return locationConstraint.getCustomerCoordinates();
    }

    public double[][] getDistanceMatrix() {
        if (distanceMatrixConstraint == null) {
            return null;
        }
        return distanceMatrixConstraint.getDistanceMatrix();
    }

    public MinMaxEnum getMinMaxEnum() {
        if (minMaxObjective == null) {
            return null;
        }
        return minMaxObjective.getMinMaxEnum();
    }

}
