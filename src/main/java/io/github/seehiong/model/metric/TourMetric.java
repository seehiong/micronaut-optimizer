package io.github.seehiong.model.metric;

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
public class TourMetric implements Metric {

    private int[] tours;
}
