package io.github.seehiong.model.metric;

import java.util.ArrayList;

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
public class VehicleRouteMetric implements Metric {

    private ArrayList<Integer>[] routes;

    public VehicleRouteMetric(int numVehicles) {
        routes = new ArrayList[numVehicles];
        for (int i = 0; i < numVehicles; i++) {
            routes[i] = new ArrayList<>();
        }
    }
}
