package io.github.seehiong.solver;

import java.util.HashMap;
import java.util.Map;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import io.github.seehiong.model.SolverState;
import io.github.seehiong.model.input.FLPInput;
import io.github.seehiong.model.metadata.LocationMetadata;
import io.github.seehiong.model.metric.AssignmentMetric;
import io.github.seehiong.model.metric.CostMetric;
import io.github.seehiong.model.output.FLPOutput;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
public class FLPSolver implements Solver<FLPInput, FLPOutput> {

    private static final MPSolver solver;

    static {
        Loader.loadNativeLibraries();
        solver = MPSolver.createSolver("SCIP");
    }

    @Override
    public Flux<Object> solve(FLPInput input, PublishSubject<FLPOutput> progressSubject) {
        return Flux.create(emitter -> {

            log.info("starting solving for: {}", input.getSolverId());
            emitter.next(FLPOutput.builder()
                    .message(String.format("start solving for %s", input.getSolverId()))
                    .build());

            int numFacility = input.getFacilityCoordinates().length;
            int numCustomer = input.getCustomerCoordinates().length;

            // Decision Variables: f[j] is 1 if facility j is opened, otherwise 0
            MPVariable[] f = solver.makeIntVarArray(numFacility, 0.0, 1.0, "f_");

            // Decision Variables: a[i][j] is 1 when customer i is assigned to facility j, otherwise 0
            MPVariable[][] a = new MPVariable[numCustomer][numFacility];
            for (int i = 0; i < numCustomer; i++) {
                a[i] = solver.makeIntVarArray(numFacility, 0.0, 1.0, "a_" + i);
            }

            // Customer constraint: Each customer is assigned to exactly one facility
            for (int i = 0; i < numCustomer; i++) {
                MPConstraint constraint = solver.makeConstraint(1.0, 1.0, "customer_" + i);
                for (int j = 0; j < numFacility; j++) {
                    constraint.setCoefficient(a[i][j], 1.0);
                }
            }

            // Capacity constraint: Facility capacities
            for (int j = 0; j < numFacility; j++) {
                MPConstraint constraint = solver.makeConstraint(-MPSolver.infinity(), 0.0, "capacity_" + j);
                for (int i = 0; i < numCustomer; i++) {
                    constraint.setCoefficient(a[i][j], input.getDemand()[i]);
                }
                constraint.setCoefficient(f[j], -input.getCapacity()[j]);
            }

            // Objective function: minimize total cost (including setup and delivery cost)
            MPObjective objective = solver.objective();
            for (int j = 0; j < numFacility; j++) {
                objective.setCoefficient(f[j], input.getSetupCost()[j]);
                for (int i = 0; i < numCustomer; i++) {
                    objective.setCoefficient(a[i][j], input.getDistanceMatrix()[i][j]);
                }
            }
            objective.setMinimization();

            // Set solver parameters
            solver.enableOutput();
            solver.setTimeLimit(1200000);

            // Solve the problem
            MPSolver.ResultStatus resultStatus = solver.solve();

            // Prepare the solution
            Map<String, Double> variables = new HashMap<>();
            AssignmentMetric assignmentMetric = new AssignmentMetric(new int[numCustomer]);
            if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
                for (int j = 0; j < numFacility; ++j) {
                    if (f[j].solutionValue() > 0.5) {
                        for (int i = 0; i < numCustomer; ++i) {
                            if (a[i][j].solutionValue() > 0.0) {
                                variables.put("a_" + i + "_" + j, a[i][j].solutionValue());
                            }
                        }
                        variables.put("f_" + j, f[j].solutionValue());
                    }
                }
                log.info("solution: {}", variables);

                for (int i = 0; i < numCustomer; ++i) {
                    for (int j = 0; j < numFacility; ++j) {
                        if (a[i][j].solutionValue() > 0.0) {
                            assignmentMetric.getFacilityAssignment()[i] = j;
                            log.debug("customer {} assigned to facility {}", i, j);
                        }
                    }
                }
                log.info("solverId: {}", input.getSolverId());

                FLPOutput optimalSolution = FLPOutput.builder()
                        .solverId(input.getSolverId())
                        .assignmentMetric(assignmentMetric)
                        .iteration((int) solver.iterations())
                        .costMetric(new CostMetric(objective.value()))
                        .build();
                emitter.next(optimalSolution);

                LocationMetadata locationMetadata = new LocationMetadata(input.getFacilityCoordinates(),
                        input.getCustomerCoordinates());
                optimalSolution.setSolverState(SolverState.SOLVED);
                optimalSolution.setLocationMetadata(locationMetadata);
                progressSubject.onNext(optimalSolution);
            }

            emitter.complete();
            progressSubject.onComplete();

        }).subscribeOn(Schedulers.boundedElastic());
    }

}
