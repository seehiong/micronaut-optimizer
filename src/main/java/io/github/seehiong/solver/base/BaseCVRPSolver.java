package io.github.seehiong.solver.base;

import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.input.CVRPInput;
import io.github.seehiong.model.metadata.CustomerCoordinateMetadata;
import io.github.seehiong.model.output.CVRPOutput;
import io.github.seehiong.utils.CoordUtil;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public abstract class BaseCVRPSolver extends BaseSolver<CVRPInput, CVRPOutput> {

    protected CustomerCoordinateMetadata customerCoord;

    @Override
    protected CVRPOutput createOutput() {
        return CVRPOutput.builder().build();
    }

    @Override
    protected CVRPOutput startSolve(CVRPInput input) {
        CVRPOutput output = super.startSolve(input);
        populateCustomerCoordinateMetadata(input);
        output.setCustomerCoordinateMetadata(customerCoord);
        return output;
    }

    private void populateCustomerCoordinateMetadata(CVRPInput input) {
        Coordinate[] coordinates;
        if (input.getCoordinates() != null) {
            coordinates = input.getCoordinates();
        } else {
            coordinates = CoordUtil.getCoordinates(input.getDistances());
        }
        customerCoord = new CustomerCoordinateMetadata(coordinates);
    }

}
