package io.github.seehiong.service;

import java.io.IOException;
import java.util.List;

import io.github.seehiong.controller.ProgressController;
import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.constraint.CustomerCoordinateConstraint;
import io.github.seehiong.model.constraint.CustomerDemandConstraint;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.VehicleConstraint;
import io.github.seehiong.model.input.CVRPInput;
import io.github.seehiong.model.output.CVRPOutput;
import io.github.seehiong.solver.CVRPSolver;
import io.github.seehiong.solver.Solver;
import io.github.seehiong.utils.DisposableUtil;
import io.github.seehiong.utils.FileUtil;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.serde.ObjectMapper;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;

@Singleton
@Named("CVRP")
public class CVRPService extends BaseSolverService<CVRPInput, CVRPOutput> {

    private final ObjectMapper objectMapper;
    private final Solver<CVRPInput, CVRPOutput> solver;

    public CVRPService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.solver = new CVRPSolver();
    }

    @Override
    public CVRPInput processInput(String input) throws IOException {
        return objectMapper.readValue(input, CVRPInput.class);
    }

    @Override
    public CVRPInput processFile(CompletedFileUpload file) throws IOException {
        List<String> lines = FileUtil.readFile(file);
        return processLines(lines);
    }

    @Override
    public Flux<Object> solve(CVRPInput input) {
        PublishSubject<CVRPOutput> progressSubject = PublishSubject.create();
        ProgressController.activeSolvers.put(input.getSolverId().toString(), progressSubject);

        Disposable subscription = progressSubject
                .doOnComplete(() -> {
                    ProgressController.activeSolvers.remove(input.getSolverId().toString());
                    DisposableUtil.disposeSubscriptions();
                })
                .subscribe(output -> ProgressController.latestOutputs.put(input.getSolverId().toString(), output));
        DisposableUtil.addDisposable(subscription);

        return solver.solve(input, (PublishSubject<CVRPOutput>) progressSubject);
    }

    private CVRPInput processLines(List<String> lines) {
        // Extract the number of customers, vehicles and its capactiy from the first line
        String[] firstLine = lines.get(0).split(" ");
        int numCustomers = Integer.parseInt(firstLine[0]);
        int numVehicles = Integer.parseInt(firstLine[1]);
        long vehicleCapacity = Long.parseLong(firstLine[2]);

        // Setup the constraints
        VehicleConstraint vehicleConstraint = new VehicleConstraint(numVehicles, vehicleCapacity);
        CustomerDemandConstraint customerDemandConstraint = new CustomerDemandConstraint(new int[numCustomers]);
        CustomerCoordinateConstraint customerCoordinateConstraint = new CustomerCoordinateConstraint(new Coordinate[numCustomers]);
        DistanceMatrixConstraint distanceMatrixConstraint = new DistanceMatrixConstraint(new double[numCustomers][numCustomers]);

        // Parse customer data
        for (int l = 1, i = 0; i < numCustomers; l++, i++) {
            String[] customerData = lines.get(l).split(" ");
            customerDemandConstraint.getDemands()[i] = Integer.parseInt(customerData[0]);
            customerCoordinateConstraint.getCoordinates()[i] = new Coordinate(Double.parseDouble(customerData[1]), Double.parseDouble(customerData[2]));
        }

        // Calculate distances
        for (int i = 0; i < numCustomers; i++) {
            for (int j = 0; j < numCustomers; j++) {
                distanceMatrixConstraint.getDistances()[i][j]
                        = calculateDistance(customerCoordinateConstraint.getCoordinates()[i], customerCoordinateConstraint.getCoordinates()[j]);
            }
        }

        return CVRPInput.builder()
                .vehicleConstraint(vehicleConstraint)
                .customerDemandConstraint(customerDemandConstraint)
                .customerCoordinateConstraint(customerCoordinateConstraint)
                .distanceMatrixConstraint(distanceMatrixConstraint)
                .build();
    }
}
