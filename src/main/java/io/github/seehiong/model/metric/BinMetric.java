package io.github.seehiong.model.metric;

import java.util.List;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BinMetric implements Metric {

    private List<Integer> weight;
    private List<List<Integer>> items;
}
