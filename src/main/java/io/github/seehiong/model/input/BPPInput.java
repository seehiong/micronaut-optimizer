package io.github.seehiong.model.input;

import io.github.seehiong.model.constraint.BinCapacityConstraint;
import io.github.seehiong.model.constraint.ItemWeightConstraint;
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
public class BPPInput extends Input {

    private ItemWeightConstraint itemWeightConstraint; // Weight of each item
    private BinCapacityConstraint binCapacityConstraint; // Capacity of each bin

    public BPPInput(ItemWeightConstraint itemWeightConstraint, BinCapacityConstraint binCapacityConstraint) {
        super();
        this.itemWeightConstraint = itemWeightConstraint;
        this.binCapacityConstraint = binCapacityConstraint;
    }

    public int[] getWeights() {
        if (itemWeightConstraint == null) {
            return null;
        }
        return itemWeightConstraint.getWeights();
    }

    public int getCapacity() {
        if (binCapacityConstraint == null) {
            return 0;
        }
        return binCapacityConstraint.getCapacity();
    }
}
