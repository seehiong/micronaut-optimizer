package io.github.seehiong.solver;

import java.util.HashMap;
import java.util.Map;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;

import io.github.seehiong.model.SolverState;
import io.github.seehiong.model.input.TSPInput;
import io.github.seehiong.model.metadata.CitiesMetadata;
import io.github.seehiong.model.metric.CostMetric;
import io.github.seehiong.model.metric.TourMetric;
import io.github.seehiong.model.objective.MinMaxEnum;
import io.github.seehiong.model.output.TSPOutput;
import io.github.seehiong.utils.CoordUtil;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
public class TSPSolver implements Solver<TSPInput, TSPOutput> {

    @Override
    public Flux<Object> solve(TSPInput input, PublishSubject<TSPOutput> progressSubject) {
        return Flux.create(emitter -> {

            log.info("Starting solving for: {}", input.getSolverId());
            emitter.next(TSPOutput.builder()
                    .message(String.format("start solving for %s", input.getSolverId()))
                    .build());

            double[][] distances = input.getDistanceMatrix();
            Map<CostMetric, TourMetric> optimalSolution = new HashMap<>();
            TourMetric bestTour = new TourMetric(new int[distances.length]);
            CostMetric bestDistance = new CostMetric(Double.MAX_VALUE);
            CitiesMetadata cities = new CitiesMetadata(CoordUtil.deriveCoordinates(distances));

            int n = distances.length;
            Model model = new Model("TSP");
            if (input.getSolveTime() != null) {
                model.getSolver().limitTime(input.getSolveTime());
            }

            // Variables
            IntVar[] tour = model.intVarArray("tour", n, 0, n - 1); // Tour representing the order of cities visited
            IntVar[] distance = model.intVarArray("distance", n, 0, 1000000); // Auxiliary variables for distances
            IntVar totalDistance = model.intVar("totalDistance", 0, 1000000); // Total distance traveled

            // Constraints
            // Define the distances between cities using table constraints
            for (int i = 0; i < n; i++) {
                Tuples tuples = new Tuples(true); // Create tuples to represent valid combinations of city and distance
                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        tuples.add(j, (int) distances[i][j]); // Add valid combinations of city and distance to tuples
                    }
                }
                model.table(tour[i], distance[i], tuples).post(); // Apply table constraint for each city
            }

            // Ensure that the tour forms a single circuit, visiting each city exactly once
            model.subCircuit(tour, 0, model.intVar(n)).post();

            // Define the objective: minimize the total distance traveled
            model.sum(distance, "=", totalDistance).post();

            if (input.getMinMaxEnum() != null) {
                MinMaxEnum minMaxEnum = input.getMinMaxEnum();
                model.setObjective(minMaxEnum == MinMaxEnum.MAXIMIZE ? Model.MAXIMIZE : Model.MINIMIZE, totalDistance);
            } else {
                model.setObjective(Model.MINIMIZE, totalDistance);
            }

            // Solver setup
            org.chocosolver.solver.Solver solver = model.getSolver();
            solver.setSearch(
                    Search.intVarSearch(
                            new FirstFail(model), // Use FirstFail search strategy to select variables
                            new IntDomainMin(), // Priorities smaller values from domain of integer variables during search
                            distance));

            while (solver.solve()) {
                int[] optimalTour = new int[n + 1];
                int current = 0; // Start from the first city
                optimalTour[0] = current;
                for (int i = 1; i <= n; i++) {
                    int next = tour[current].getValue(); // Get the next city in the tour
                    optimalTour[i] = next;
                    log.debug("current: {}, next: {}", current, next); // Print the current and next city
                    current = tour[current].getValue(); // Move to the next city
                }

                if (totalDistance.getValue() < bestDistance.getCost()) {
                    bestDistance.setCost(totalDistance.getValue());
                    bestTour.setTour(optimalTour);
                    optimalSolution.put(bestDistance, bestTour);
                    log.info("best tour distance: {}", bestDistance); // Print the new best distance

                    TSPOutput bestOutput = TSPOutput.builder()
                            .iteration((int) model.getSolver().getSolutionCount())
                            .costMetric(bestDistance)
                            .build();
                    emitter.next(bestOutput);

                    bestOutput.setSolverState(SolverState.SOLVING);
                    bestOutput.setCitiesMetadata(cities);
                    bestOutput.setTourMetric(optimalSolution.get(bestDistance));
                    progressSubject.onNext(bestOutput);
                }
            }

            if (model.getSolver().hasObjective()) {
                bestDistance.setCost(model.getSolver().getBestSolutionValue().doubleValue());
                log.info("optimal tour distance: {}", bestDistance);

                TSPOutput optimalOutput = TSPOutput.builder()
                        .solverId(input.getSolverId())
                        .iteration((int) model.getSolver().getSolutionCount())
                        .tourMetric(optimalSolution.get(bestDistance))
                        .costMetric(bestDistance)
                        .build();
                emitter.next(optimalOutput);

                optimalOutput.setSolverState(SolverState.SOLVED);
                optimalOutput.setCitiesMetadata(cities);
                optimalOutput.setTourMetric(optimalSolution.get(bestDistance));
                progressSubject.onNext(optimalOutput);

            }

            emitter.complete();
            progressSubject.onComplete();

        }).subscribeOn(Schedulers.boundedElastic());
    }

}
