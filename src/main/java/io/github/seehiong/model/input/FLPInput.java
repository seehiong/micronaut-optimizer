package io.github.seehiong.model.input;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.constraint.CustomerCoordinateConstraint;
import io.github.seehiong.model.constraint.CustomerDemandConstraint;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.FacilityCapacityConstraint;
import io.github.seehiong.model.constraint.FacilityCoordinateConstraint;
import io.github.seehiong.model.constraint.FacilityCostConstraint;
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
    private FacilityCoordinateConstraint facilityCoordinateConstraint;
    private CustomerDemandConstraint customerDemandConstraint;
    private CustomerCoordinateConstraint customerCoordinateConstraint;
    private DistanceMatrixConstraint distanceMatrixConstraint;
    private MinMaxObjective minMaxObjective;

    public FLPInput(FacilityCostConstraint facilityCostConstraint,
            FacilityCapacityConstraint facilityCapacityConstraint, FacilityCoordinateConstraint facilityCoordinateConstraint,
            CustomerDemandConstraint customerDemandConstraint, CustomerCoordinateConstraint customerCoordinateConstraint,
            DistanceMatrixConstraint distanceMatrixConstraint, MinMaxObjective minMaxObjective) {
        super();
        this.facilityCostConstraint = facilityCostConstraint;
        this.facilityCapacityConstraint = facilityCapacityConstraint;
        this.facilityCoordinateConstraint = facilityCoordinateConstraint;
        this.customerDemandConstraint = customerDemandConstraint;
        this.customerCoordinateConstraint = customerCoordinateConstraint;
        this.distanceMatrixConstraint = distanceMatrixConstraint;
        this.minMaxObjective = minMaxObjective;
    }

    public double[] getCosts() {
        if (facilityCostConstraint == null) {
            return null;
        }
        return facilityCostConstraint.getCosts();
    }

    public int[] getCapacities() {
        if (facilityCapacityConstraint == null) {
            return null;
        }
        return facilityCapacityConstraint.getCapacities();
    }

    public int[] getDemands() {
        if (customerDemandConstraint == null) {
            return null;
        }
        return customerDemandConstraint.getDemands();
    }

    public Coordinate[] getFacilityCoordinates() {
        if (facilityCoordinateConstraint == null) {
            return null;
        }
        return facilityCoordinateConstraint.getCoordinates();
    }

    public Coordinate[] getCustomerCoordinates() {
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

    public MinMaxEnum getMinMaxEnum() {
        if (minMaxObjective == null) {
            return null;
        }
        return minMaxObjective.getMinMaxEnum();
    }
}
