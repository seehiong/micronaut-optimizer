package io.github.seehiong.service;

import java.io.IOException;
import java.util.List;

import io.github.seehiong.controller.ProgressController;
import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.constraint.CustomerCoordinateConstraint;
import io.github.seehiong.model.constraint.CustomerDemandConstraint;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.FacilityCapacityConstraint;
import io.github.seehiong.model.constraint.FacilityCoordinateConstraint;
import io.github.seehiong.model.constraint.FacilityCostConstraint;
import io.github.seehiong.model.input.FLPInput;
import io.github.seehiong.model.output.FLPOutput;
import io.github.seehiong.solver.FLPSolver;
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
@Named("FLP")
public class FLPService extends BaseSolverService<FLPInput, FLPOutput> {

    private final ObjectMapper objectMapper;
    private final Solver<FLPInput, FLPOutput> solver;

    public FLPService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.solver = new FLPSolver();
    }

    @Override
    public FLPInput processInput(String input) throws IOException {
        FLPInput fLPInput = objectMapper.readValue(input, FLPInput.class);
        if (fLPInput != null && fLPInput.getDistances() == null) {
            int numCustomers = fLPInput.getCustomerCoordinates().length;
            int numFacilities = fLPInput.getFacilityCoordinates().length;
            fLPInput.setDistanceMatrixConstraint(new DistanceMatrixConstraint(
                    calculateDistances(numCustomers, numFacilities, fLPInput.getCustomerCoordinateConstraint(), fLPInput.getFacilityCoordinateConstraint())));
        }
        return fLPInput;
    }

    @Override
    public FLPInput processFile(CompletedFileUpload file) throws IOException {
        List<String> lines = FileUtil.readFile(file);
        return processLines(lines);
    }

    @Override
    public Flux<Object> solve(FLPInput input) {
        PublishSubject<FLPOutput> progressSubject = PublishSubject.create();
        ProgressController.activeSolvers.put(input.getSolverId().toString(), progressSubject);

        Disposable subscription;
        subscription = progressSubject
                .doOnComplete(() -> {
                    ProgressController.activeSolvers.remove(input.getSolverId().toString());
                    DisposableUtil.disposeSubscriptions();
                })
                .subscribe(output -> ProgressController.latestOutputs.put(input.getSolverId().toString(), output));
        DisposableUtil.addDisposable(subscription);

        return solver.solve(input, (PublishSubject<FLPOutput>) progressSubject);
    }

    private FLPInput processLines(List<String> lines) {
        // Extract the number of facilities and customers from the first line
        String[] firstLine = lines.get(0).split(" ");
        int numFacilities = Integer.parseInt(firstLine[0]);
        int numCustomers = Integer.parseInt(firstLine[1]);

        // Setup the constraints
        FacilityCostConstraint facilityCostConstraint = new FacilityCostConstraint(new double[numFacilities]);
        FacilityCapacityConstraint facilityCapacityConstraint = new FacilityCapacityConstraint(new int[numFacilities]);
        FacilityCoordinateConstraint facilityCoordinateConstraint = new FacilityCoordinateConstraint(new Coordinate[numFacilities]);
        CustomerDemandConstraint customerDemandConstraint = new CustomerDemandConstraint(new int[numCustomers]);
        CustomerCoordinateConstraint customerCoordinateConstraint = new CustomerCoordinateConstraint(new Coordinate[numCustomers]);
        DistanceMatrixConstraint distanceMatrixConstraint = new DistanceMatrixConstraint(new double[numCustomers][numFacilities]);

        // Parse facility data
        for (int l = 1, i = 0; i < numFacilities; l++, i++) {
            String[] facilityData = lines.get(l).split(" ");
            facilityCostConstraint.getCosts()[i] = Double.parseDouble(facilityData[0]);
            facilityCapacityConstraint.getCapacities()[i] = Integer.parseInt(facilityData[1]);
            facilityCoordinateConstraint.getCoordinates()[i] = new Coordinate(Double.parseDouble(facilityData[2]), Double.parseDouble(facilityData[3]));
        }

        // Parse customer data
        for (int l = numFacilities + 1, i = 0; i < numCustomers; l++, i++) {
            String[] customerData = lines.get(l).split(" ");
            customerDemandConstraint.getDemands()[i] = Integer.parseInt(customerData[0]);
            customerCoordinateConstraint.getCoordinates()[i] = new Coordinate(Double.parseDouble(customerData[1]), Double.parseDouble(customerData[2]));
        }

        distanceMatrixConstraint.setDistances(calculateDistances(numCustomers, numFacilities, customerCoordinateConstraint, facilityCoordinateConstraint));

        return FLPInput.builder()
                .facilityCapacityConstraint(facilityCapacityConstraint)
                .facilityCostConstraint(facilityCostConstraint)
                .facilityCoordinateConstraint(facilityCoordinateConstraint)
                .customerDemandConstraint(customerDemandConstraint)
                .customerCoordinateConstraint(customerCoordinateConstraint)
                .distanceMatrixConstraint(distanceMatrixConstraint)
                .build();
    }

    private double[][] calculateDistances(int numCustomers, int numFacilities,
            CustomerCoordinateConstraint customerCoordinateConstraint, FacilityCoordinateConstraint facilityCoordinateConstraint) {
        double[][] distances = new double[numCustomers][numFacilities];
        // Calculate distances
        for (int i = 0; i < numCustomers; i++) {
            for (int j = 0; j < numFacilities; j++) {
                distances[i][j]
                        = calculateDistance(customerCoordinateConstraint.getCoordinates()[i], facilityCoordinateConstraint.getCoordinates()[j]);
            }
        }
        return distances;
    }
}
