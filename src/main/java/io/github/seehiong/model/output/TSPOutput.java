package io.github.seehiong.model.output;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.seehiong.model.metadata.CitiesMetadata;
import io.github.seehiong.model.metric.CostMetric;
import io.github.seehiong.model.metric.TourMetric;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TSPOutput extends Output {

    private TourMetric tourMetric; // List of tours
    private CostMetric costMetric; // Total cost or length of the hamiltonian cycle
    private CitiesMetadata citiesMetadata; // Coordinates of cities

    public TSPOutput(TourMetric tourMetric, CostMetric costMetric, CitiesMetadata citiesMetadata) {
        super();
        this.tourMetric = tourMetric;
        this.costMetric = costMetric;
        this.citiesMetadata = citiesMetadata;
    }

    @JsonIgnore
    public int[] getTours() {
        if (tourMetric == null) {
            return null;
        }
        return tourMetric.getTours();
    }

    @JsonIgnore
    public double getCost() {
        if (costMetric == null) {
            return 0;
        }
        return costMetric.getCost();
    }

    @JsonIgnore
    public double[][] getCities() {
        if (citiesMetadata == null) {
            return null;
        }
        return citiesMetadata.getCities();
    }
}
