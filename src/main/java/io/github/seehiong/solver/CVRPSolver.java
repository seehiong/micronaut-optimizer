package io.github.seehiong.solver;

import java.util.HashMap;
import java.util.Map;

import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.LocalSearchMetaheuristic;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;
import com.google.protobuf.Duration;

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
public class CVRPSolver extends BaseCVRPSolver {

    @Override
    public Flux<Object> solve(CVRPInput input, PublishSubject<CVRPOutput> publisher) {
        return Flux.create(emitter -> {
            CVRPOutput output = super.startSolve(input);
            super.publishNext(emitter, publisher, output);

            RoutingIndexManager manager = new RoutingIndexManager(input.getDistances().length, input.getVehicleNumber(), 0); //defaults to 0 for depot
            // Create Routing Model
            RoutingModel routing = new RoutingModel(manager);

            // Create and register a transit callback
            final int transitCallbackIndex
                    = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                        // Convert from routing variable Index to user NodeIndex.
                        int fromNode = manager.indexToNode(fromIndex);
                        int toNode = manager.indexToNode(toIndex);
                        return (long) input.getDistances()[fromNode][toNode];
                    });

            // Define cost of each arc.
            routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

            // Add Capacity constraint.
            final int demandCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
                // Convert from routing variable Index to user NodeIndex.
                int fromNode = manager.indexToNode(fromIndex);
                return input.getDemands()[fromNode];
            });
            routing.addDimensionWithVehicleCapacity(demandCallbackIndex, 0, // null capacity slack
                    input.getCapacities(), // vehicle maximum capacities
                    true, // start cumul to zero
                    "Capacity");

            // Setting first solution heuristic.
            RoutingSearchParameters searchParameters;
            searchParameters = main.defaultRoutingSearchParameters()
                    .toBuilder()
                    .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                    .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                    .setTimeLimit(Duration.newBuilder().setSeconds(input.getTimeInSeconds()).build())
                    .build();

            // Solve the problem.
            Assignment solution = routing.solveWithParameters(searchParameters);
            VehicleRouteMetric routes = new VehicleRouteMetric(input.getVehicleNumber());

            if (solution != null) {
                Map<Integer, String> vehicleMap = new HashMap<>();
                for (int i = 0; i < input.getVehicleNumber(); ++i) {
                    long index = routing.start(i);
                    long routeLoad = 0;
                    String route = "";
                    while (!routing.isEnd(index)) {
                        long nodeIndex = manager.indexToNode(index);
                        routeLoad += input.getDemands()[(int) nodeIndex];
                        route += nodeIndex + " Load(" + routeLoad + ") -> ";
                        index = solution.value(routing.nextVar(index));
                        routes.getRoutes()[i].add((int) nodeIndex);
                    }
                    route += manager.indexToNode(routing.end(i));
                    vehicleMap.put(i, route);
                    routes.getRoutes()[i].add(0); //return to depot
                }

                String message = String.format("vehicleMap: %s", vehicleMap.toString());
                log.info(message);

                super.publishNext(emitter, publisher, CVRPOutput.builder()
                        .solverId(input.getSolverId())
                        .solverState(SolverState.SOLVED)
                        .message(message)
                        .vehicleRouteMetric(routes)
                        .costMetric(new CostMetric(solution.objectiveValue()))
                        .customerCoordinateMetadata(super.customerCoord)
                        .build());

            } else {
                emitter.error(new RuntimeException("No solution found"));
            }

            super.publishComplete(emitter, publisher);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
