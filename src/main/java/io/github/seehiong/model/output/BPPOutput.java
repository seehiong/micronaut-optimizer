package io.github.seehiong.model.output;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.seehiong.model.metric.BinMetric;
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
public class BPPOutput extends Output {

    private BinMetric binMetric; // List of bins and their items

    public BPPOutput(BinMetric binMetric) {
        super();
        this.binMetric = binMetric;
    }

    @JsonIgnore
    public List<Integer> getWeight() {
        if (binMetric == null) {
            return null;
        }
        return binMetric.getWeight();
    }

    @JsonIgnore
    public List<List<Integer>> getItems() {
        if (binMetric == null) {
            return null;
        }
        return binMetric.getItems();
    }
}
