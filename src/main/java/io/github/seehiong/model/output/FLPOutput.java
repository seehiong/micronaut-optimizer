package io.github.seehiong.model.output;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.metadata.FacilityCoordinateMetadata;
import io.github.seehiong.model.metadata.CustomerCoordinateMetadata;
import io.github.seehiong.model.metric.AssignmentMetric;
import io.github.seehiong.model.metric.CostMetric;
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
public class FLPOutput extends Output {

    private AssignmentMetric assignmentMetric; // List of facility assignments for each customer
    private CostMetric costMetric; // Total setup cost of the facility
    private FacilityCoordinateMetadata facilityCoordinateMetadata; // Coordinates of facilities
    private CustomerCoordinateMetadata customerCoordinateMetadata; // Coordinates of customers

    public FLPOutput(AssignmentMetric assignmentMetric, CostMetric costMetric,
            FacilityCoordinateMetadata facilityCoordinateMetadata, CustomerCoordinateMetadata customerCoordinateMetadata) {
        super();
        this.assignmentMetric = assignmentMetric;
        this.costMetric = costMetric;
        this.facilityCoordinateMetadata = facilityCoordinateMetadata;
        this.customerCoordinateMetadata = customerCoordinateMetadata;
    }

    @JsonIgnore
    public int[] getAssignments() {
        if (assignmentMetric == null) {
            return null;
        }
        return assignmentMetric.getAssignments();
    }

    @JsonIgnore
    public double getCost() {
        if (costMetric == null) {
            return 0;
        }
        return costMetric.getCost();
    }

    @JsonIgnore
    public Coordinate[] getFacilityCoordinates() {
        if (facilityCoordinateMetadata == null) {
            return null;
        }
        return facilityCoordinateMetadata.getCoordinates();
    }

    @JsonIgnore
    public Coordinate[] getCustomerCoordinates() {
        if (customerCoordinateMetadata == null) {
            return null;
        }
        return customerCoordinateMetadata.getCoordinates();
    }
}
