package io.github.seehiong.model.output;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.metadata.CustomerCoordinateMetadata;
import io.github.seehiong.model.metadata.FacilityCoordinateMetadata;
import io.github.seehiong.model.metric.CostMetric;
import io.github.seehiong.model.metric.VehicleRouteMetric;
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
public class CVRPOutput extends Output {

    private VehicleRouteMetric vehicleRouteMetric; // List of routes for each vehicle
    private CostMetric costMetric; // Total cost or length of the routes
    private CustomerCoordinateMetadata customerCoordinateMetadata; // Coordinates of customers

    public CVRPOutput(VehicleRouteMetric vehicleRouteMetric, CostMetric costMetric,
            CustomerCoordinateMetadata customerCoordinateMetadata, FacilityCoordinateMetadata facilityCoordinateMetadata) {
        super();
        this.vehicleRouteMetric = vehicleRouteMetric;
        this.costMetric = costMetric;
        this.customerCoordinateMetadata = customerCoordinateMetadata;
    }

    @JsonIgnore
    public ArrayList<Integer>[] getRoutes() {
        if (vehicleRouteMetric == null) {
            return null;
        }
        return vehicleRouteMetric.getRoutes();
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
        if (customerCoordinateMetadata == null) {
            return null;
        }
        return customerCoordinateMetadata.getCoordinates();
    }
}
