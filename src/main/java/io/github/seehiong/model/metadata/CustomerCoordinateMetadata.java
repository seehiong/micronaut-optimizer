package io.github.seehiong.model.metadata;

import io.github.seehiong.model.Coordinate;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Introspected
@Serdeable.Serializable
@Serdeable.Deserializable
@Data
@NoArgsConstructor
public class CustomerCoordinateMetadata implements Metadata {

    private Coordinate[] coordinates;

    public CustomerCoordinateMetadata(Coordinate[] coordinates) {
        this.coordinates = coordinates;
    }

}
