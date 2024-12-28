package io.github.seehiong.model.output;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.metadata.LocationMetadata;
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

    private AssignmentMetric assignmentMetric;
    private CostMetric costMetric;
    private LocationMetadata locationMetadata;

    public FLPOutput(AssignmentMetric assignmentMetric, CostMetric costMetric, LocationMetadata locationMetadata) {
        super();
        this.assignmentMetric = assignmentMetric;
        this.costMetric = costMetric;
        this.locationMetadata = locationMetadata;
    }

    @JsonIgnore
    public int[] getFacilityAssignment() {
        if (assignmentMetric == null) {
            return null;
        }
        return assignmentMetric.getFacilityAssignment();
    }

    @JsonIgnore
    public double getCost() {
        if (costMetric == null) {
            return 0;
        }
        return costMetric.getCost();
    }

    @JsonIgnore
    public Coordinate[] getCustomerCoordinates() {
        if (locationMetadata == null) {
            return null;
        }
        return locationMetadata.getCustomerCoordinates();
    }

    @JsonIgnore
    public Coordinate[] getFacilityCoordinates() {
        if (locationMetadata == null) {
            return null;
        }
        return locationMetadata.getFacilityCoordinates();
    }

}
