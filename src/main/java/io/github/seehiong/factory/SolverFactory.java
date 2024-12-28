package io.github.seehiong.factory;

import io.github.seehiong.model.ProblemType;
import io.github.seehiong.solver.FLPSolver;
import io.github.seehiong.solver.Solver;
import io.github.seehiong.solver.TSPGaSolver;
import io.github.seehiong.solver.TSPSolver;
import jakarta.inject.Singleton;

@Singleton
public class SolverFactory {

    public Solver<?, ?> getSolver(ProblemType problemType) {
        return switch (problemType) {
            case TSP ->
                new TSPSolver();
            case TSP_GA ->
                new TSPGaSolver();
            case FLP ->
                new FLPSolver();
            default ->
                throw new IllegalArgumentException("Unknown problem type: " + problemType);
        };
    }

}
