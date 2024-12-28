package io.github.seehiong.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.seehiong.factory.SolverFactory;
import io.github.seehiong.model.Coordinate;
import io.github.seehiong.model.constraint.CustomerDemandConstraint;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.constraint.FacilityCapacityConstraint;
import io.github.seehiong.model.constraint.FacilityCostConstraint;
import io.github.seehiong.model.constraint.LocationConstraint;
import io.github.seehiong.model.input.FLPInput;
import io.github.seehiong.model.output.FLPOutput;
import io.github.seehiong.solver.Solver;
import io.github.seehiong.utils.DisposableUtil;
import io.github.seehiong.utils.FileUtil;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.sse.Event;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Singleton
@RequiredArgsConstructor
public class FLPService {

    public static final Map<String, PublishSubject<FLPOutput>> activeSolvers = new ConcurrentHashMap<>();
    public static final Map<String, FLPOutput> latestOutputs = new ConcurrentHashMap<>();

    private final SolverFactory solverFactory;

    public Flux<Object> uploadSolve(FLPInput input, CompletedFileUpload file) throws IOException {
        List<String> lines = FileUtil.readFile(file);
        processLines(lines, input);
        return solve(input);
    }

    private Flux<Object> solve(FLPInput input) {
        Solver<FLPInput, FLPOutput> solver = (Solver<FLPInput, FLPOutput>) solverFactory.getSolver(input.getProblemType());
        PublishSubject<FLPOutput> progressSubject = PublishSubject.create();
        activeSolvers.put(input.getSolverId().toString(), progressSubject);

        Disposable subscription = progressSubject
                .doOnComplete(() -> {
                    activeSolvers.remove(input.getSolverId().toString());
                    DisposableUtil.disposeSubscriptions();
                })
                .subscribe(output -> latestOutputs.put(input.getSolverId().toString(), output));
        DisposableUtil.addDisposable(subscription);

        return solver.solve(input, (PublishSubject<FLPOutput>) progressSubject);
    }

    public FLPOutput getLatestOutput(String solverId) {
        return latestOutputs.get(solverId);
    }

    public Flowable<Event<FLPOutput>> streamProgress(String solverId) {
        PublishSubject<FLPOutput> progressSubject = activeSolvers.get(solverId);
        if (progressSubject == null) {
            return Flowable.empty();
        }
        return progressSubject.toFlowable(BackpressureStrategy.BUFFER).map(Event::of);
    }

    private void processLines(List<String> lines, FLPInput input) {
        // Extract the number of facilities and customers from the first line
        String[] firstLine = lines.get(0).split(" ");
        int numFacilities = Integer.parseInt(firstLine[0]);
        int numCustomers = Integer.parseInt(firstLine[1]);

        // Setup the constraints
        FacilityCostConstraint facilityCostConstraint = new FacilityCostConstraint(new double[numFacilities]);
        FacilityCapacityConstraint facilityCapacityConstraint = new FacilityCapacityConstraint(new int[numFacilities]);
        CustomerDemandConstraint customerDemandConstraint = new CustomerDemandConstraint(new int[numCustomers]);
        LocationConstraint locationConstraint = new LocationConstraint(new Coordinate[numFacilities], new Coordinate[numCustomers]);
        DistanceMatrixConstraint distanceMatrixConstraint = new DistanceMatrixConstraint(new double[numCustomers][numFacilities]);

        // Parse facility data
        for (int l = 1, i = 0; i < numFacilities; l++, i++) {
            String[] facilityData = lines.get(l).split(" ");
            facilityCostConstraint.getSetupCost()[i] = Double.parseDouble(facilityData[0]);
            facilityCapacityConstraint.getCapacity()[i] = Integer.parseInt(facilityData[1]);
            locationConstraint.getFacilityCoordinates()[i] = new Coordinate(Double.parseDouble(facilityData[2]), Double.parseDouble(facilityData[3]));
        }

        // Parse customer data
        for (int l = numFacilities + 1, i = 0; i < numCustomers; l++, i++) {
            String[] customerData = lines.get(l).split(" ");
            customerDemandConstraint.getDemand()[i] = Integer.parseInt(customerData[0]);
            locationConstraint.getCustomerCoordinates()[i] = new Coordinate(Double.parseDouble(customerData[1]), Double.parseDouble(customerData[2]));
        }

        // Calculate distances
        for (int i = 0; i < numCustomers; i++) {
            for (int j = 0; j < numFacilities; j++) {
                distanceMatrixConstraint.getDistanceMatrix()[i][j]
                        = calculateDistance(locationConstraint.getCustomerCoordinates()[i], locationConstraint.getFacilityCoordinates()[j]);
            }
        }

        input.setFacilityCapacityConstraint(facilityCapacityConstraint);
        input.setFacilityCostConstraint(facilityCostConstraint);
        input.setCustomerDemandConstraint(customerDemandConstraint);
        input.setLocationConstraint(locationConstraint);
        input.setDistanceMatrixConstraint(distanceMatrixConstraint);
    }

    private double calculateDistance(Coordinate customer, Coordinate facility) {
        double dx = customer.getX() - facility.getX();
        double dy = customer.getY() - facility.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

}
