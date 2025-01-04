package io.github.seehiong.solver;

import java.util.ArrayList;
import java.util.List;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import io.github.seehiong.model.SolverState;
import io.github.seehiong.model.input.BPPInput;
import io.github.seehiong.model.metric.BinMetric;
import io.github.seehiong.model.output.BPPOutput;
import io.github.seehiong.solver.base.BaseSolver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
public class BPPSolver extends BaseSolver<BPPInput, BPPOutput> {

    private static final MPSolver solver = MPSolver.createSolver("SCIP");

    @Override
    protected BPPOutput createOutput() {
        return BPPOutput.builder().build();
    }

    @Override
    public Flux<Object> solve(BPPInput input, PublishSubject<BPPOutput> publisher) {
        return Flux.create(emitter -> {
            BPPOutput output = super.startSolve(input);
            super.publishNext(emitter, publisher, output);

            int numItems = input.getWeights().length;
            int numBins = numItems;

            MPVariable[][] x = new MPVariable[numItems][numBins];
            for (int i = 0; i < numItems; ++i) {
                for (int j = 0; j < numBins; ++j) {
                    x[i][j] = solver.makeIntVar(0, 1, "");
                }
            }
            MPVariable[] y = new MPVariable[numBins];
            for (int j = 0; j < numBins; ++j) {
                y[j] = solver.makeIntVar(0, 1, "");
            }

            double infinity = java.lang.Double.POSITIVE_INFINITY;
            for (int i = 0; i < numItems; ++i) {
                MPConstraint constraint = solver.makeConstraint(1, 1, "");
                for (int j = 0; j < numBins; ++j) {
                    constraint.setCoefficient(x[i][j], 1);
                }
            }
            // The bin capacity contraint for bin j is
            //   sum_i w_i x_ij <= C*y_j
            // To define this constraint, first subtract the left side from the right to get
            //   0 <= C*y_j - sum_i w_i x_ij
            //
            // Note: Since sum_i w_i x_ij is positive (and y_j is 0 or 1), the right side must
            // be less than or equal to C. But it's not necessary to add this constraint
            // because it is forced by the other constraints.

            for (int j = 0; j < numBins; ++j) {
                MPConstraint constraint = solver.makeConstraint(0, infinity, "");
                constraint.setCoefficient(y[j], input.getCapacity());
                for (int i = 0; i < numItems; ++i) {
                    constraint.setCoefficient(x[i][j], -input.getWeights()[i]);
                }
            }

            MPObjective objective = solver.objective();
            for (int j = 0; j < numBins; ++j) {
                objective.setCoefficient(y[j], 1);
            }
            objective.setMinimization();

            final MPSolver.ResultStatus resultStatus = solver.solve();

            // Check that the problem has an optimal solution.
            if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
                log.info("number of bins used: {}", objective.value());
                double totalWeight = 0;

                // Use Lists to dynamically track used bins
                List<Integer> usedWeights = new ArrayList<>();
                List<List<Integer>> usedItems = new ArrayList<>();

                for (int j = 0; j < numBins; ++j) {
                    if (y[j].solutionValue() == 1) {
                        log.debug("bin {}", j);
                        double binWeight = 0;
                        List<Integer> items = new ArrayList<>();

                        for (int i = 0; i < numItems; ++i) {
                            if (x[i][j].solutionValue() == 1) {
                                log.debug("- item {}, weight: {}", i, input.getWeights()[i]);
                                binWeight += input.getWeights()[i];
                                items.add(i);
                            }
                        }
                        log.info("packed bin {}, weight: {}", j, binWeight);

                        // Add the weight and items for this bin
                        usedWeights.add((int) binWeight);
                        usedItems.add(items);

                        totalWeight += binWeight;
                    }
                }
                log.info("total packed weight: {}", totalWeight);

                BinMetric binMetric = BinMetric.builder()
                        .weight(usedWeights)
                        .items(usedItems)
                        .build();

                super.publishNext(emitter, publisher, BPPOutput.builder()
                        .solverId(input.getSolverId())
                        .solverState(SolverState.SOLVED)
                        .binMetric(binMetric)
                        .build());

            } else {
                log.info("the problem does not have an optimal solution.");
            }

            super.publishComplete(emitter, publisher);
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
