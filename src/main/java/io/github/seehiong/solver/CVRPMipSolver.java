package io.github.seehiong.solver;

import java.util.HashMap;
import java.util.Map;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import io.github.seehiong.model.SolverState;
import io.github.seehiong.model.input.CVRPInput;
import io.github.seehiong.model.metric.CostMetric;
import io.github.seehiong.model.metric.VehicleRouteMetric;
import io.github.seehiong.model.output.CVRPOutput;
import io.github.seehiong.solver.base.BaseCVRPSolver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
public class CVRPMipSolver extends BaseCVRPSolver {

    private static final MPSolver solver = new MPSolver("CVRP_MIP", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);

    @Override
    public Flux<Object> solve(CVRPInput input, PublishSubject<CVRPOutput> publisher) {
        return Flux.create(emitter -> {
            CVRPOutput output = super.startSolve(input);
            super.publishNext(emitter, publisher, output);

            final int numNodes = input.getDistances().length;
            final int numVehicles = input.getVehicleNumber();
            final int depot = 0; // Depot is always at index 0

            // Variables: t[i][j][k] is 1 if vehicle k travels from node i to node j
            MPVariable[][][] t = new MPVariable[numNodes][numNodes][numVehicles];
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    if (i != j) {
                        for (int k = 0; k < numVehicles; k++) {
                            t[i][j][k] = solver.makeIntVar(0, 1, "travel_" + i + "_" + j + "_" + k);
                        }
                    }
                }
            }

            // Variables: s[i][k] is 1 if node i is served by k
            MPVariable[][] s = new MPVariable[numNodes][numVehicles];
            for (int i = 0; i < numNodes; i++) {
                for (int k = 0; k < numVehicles; k++) {
                    s[i][k] = solver.makeIntVar(0, 1, "serve_" + i + "_" + k);
                }
            }

            // Objective: Minimize the total distance traveled
            MPObjective objective = solver.objective();
            for (int k = 0; k < numVehicles; k++) {
                for (int i = 0; i < numNodes; i++) {
                    for (int j = 0; j < numNodes; j++) {
                        if (i != j) {
                            objective.setCoefficient(t[i][j][k], input.getDistances()[i][j]);
                        }
                    }
                }
            }
            objective.setMinimization();

            // Constraints:
            // 1. Each vehicle starts at the depot
            for (int k = 0; k < numVehicles; k++) {
                MPConstraint constraint = solver.makeConstraint(1, 1, "depot_start_" + k);
                for (int j = 1; j < numNodes; j++) {
                    constraint.setCoefficient(t[depot][j][k], 1);
                }
            }

            // 2. Each vehicle ends at the depot
            for (int k = 0; k < numVehicles; k++) {
                MPConstraint constraint = solver.makeConstraint(1, 1, "deport_end_" + k);
                for (int i = 1; i < numNodes; i++) {
                    constraint.setCoefficient(t[i][depot][k], 1);
                }
            }

            // 3. Each node is visited exactly once by exactly one vehicle (excluding depot)
            for (int j = 1; j < numNodes; j++) {
                MPConstraint constraint = solver.makeConstraint(1, 1, "visit_node_" + j);
                for (int i = 0; i < numNodes; i++) {
                    if (i != j) {
                        for (int k = 0; k < numVehicles; k++) {
                            constraint.setCoefficient(t[i][j][k], 1);
                        }
                    }
                }
            }

            // 4. Flow conservation: if a vehicle arrives at a node, it must leave
            for (int k = 0; k < numVehicles; k++) {
                for (int n = 0; n < numNodes; n++) {  // Include depot to enforce return
                    MPConstraint constraint = solver.makeConstraint(0, 0, "flow_conservation_" + n + "_" + k);
                    for (int i = 0; i < numNodes; i++) {
                        if (i != n) {
                            constraint.setCoefficient(t[i][n][k], 1);
                        }
                    }
                    for (int j = 0; j < numNodes; j++) {
                        if (n != j) {
                            constraint.setCoefficient(t[n][j][k], -1);
                        }
                    }
                }
            }

            // 5. Capacity constraints
            for (int k = 0; k < numVehicles; k++) {
                MPConstraint constraint = solver.makeConstraint(0, input.getCapacities()[k], "capacity_" + k);
                for (int i = 1; i < numNodes; i++) {
                    for (int j = 1; j < numNodes; j++) {
                        if (i != j) {
                            constraint.setCoefficient(t[i][j][k], input.getDemands()[i]);
                        }
                    }
                }
            }

            // Create auxiliary variables for subtour elimination
            MPVariable[] u = new MPVariable[numNodes];
            for (int i = 1; i < numNodes; i++) {
                u[i] = solver.makeIntVar(0, numNodes, "u_" + i);
            }

            // 6. Subtour elimination constraints
            for (int k = 0; k < numVehicles; k++) {
                for (int i = 1; i < numNodes; i++) {
                    for (int j = 1; j < numNodes; j++) {
                        if (i != j) {
                            MPConstraint subtourConstraint = solver.makeConstraint(-MPSolver.infinity(), numNodes - 1, "subtour_elimination_" + i + "_" + j + "_" + k);
                            subtourConstraint.setCoefficient(u[i], 1);
                            subtourConstraint.setCoefficient(u[j], -1);
                            subtourConstraint.setCoefficient(t[i][j][k], numNodes);
                        }
                    }
                }
            }

            // Solve
            solver.setTimeLimit(input.getTimeInSeconds() * 1000); // Convert to milliseconds
            MPSolver.ResultStatus resultStatus = solver.solve();

            // Check the result
            Map<Integer, String> vehicleMap = new HashMap<>();
            VehicleRouteMetric routes = new VehicleRouteMetric(input.getVehicleNumber());

            if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
                for (int k = 0; k < numVehicles; k++) {
                    int currentNode = depot;
                    String route = String.valueOf(depot); // starts from depot
                    routes.getRoutes()[k].add(0); //starts from depot

                    while (true) {
                        boolean routeContinues = false;
                        for (int j = 0; j < numNodes; j++) {
                            if (currentNode != j && t[currentNode][j][k].solutionValue() == 1.0) {
                                route += " -> " + j;
                                currentNode = j;
                                routeContinues = true;
                                routes.getRoutes()[k].add(j);
                                break;
                            }
                        }
                        if (!routeContinues || currentNode == depot) {
                            break;  // No further route found for this vehicle
                        }
                    }
                    vehicleMap.put(k, route);
                }

                String message = String.format("vehicleMap: %s", vehicleMap.toString());
                log.info(message);

                super.publishNext(emitter, publisher, CVRPOutput.builder()
                        .solverId(input.getSolverId())
                        .solverState(SolverState.SOLVED)
                        .message(message)
                        .vehicleRouteMetric(routes)
                        .costMetric(new CostMetric(solver.objective().value()))
                        .customerCoordinateMetadata(super.customerCoord)
                        .iteration((int) solver.iterations())
                        .build());
            }

            super.publishComplete(emitter, publisher);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
