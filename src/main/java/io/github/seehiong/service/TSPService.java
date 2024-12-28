package io.github.seehiong.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.seehiong.factory.SolverFactory;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.input.TSPInput;
import io.github.seehiong.model.output.TSPOutput;
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
public class TSPService {

    private static final Map<String, PublishSubject<TSPOutput>> activeSolvers = new ConcurrentHashMap<>();
    private static final Map<String, TSPOutput> latestOutputs = new ConcurrentHashMap<>();

    private final SolverFactory solverFactory;

    public Flux<Object> uploadSolve(TSPInput input, CompletedFileUpload file) throws IOException {
        List<String> lines = FileUtil.readFile(file);
        double[][] distances = processLines(lines);

        input.setDistanceMatrixConstraint(new DistanceMatrixConstraint(distances));
        return solve(input);
    }

    public Flux<Object> solve(TSPInput input) {
        Solver<TSPInput, TSPOutput> solver = (Solver<TSPInput, TSPOutput>) solverFactory.getSolver(input.getProblemType());
        PublishSubject<TSPOutput> progressSubject = PublishSubject.create();
        activeSolvers.put(input.getSolverId().toString(), progressSubject);

        Disposable subscription = progressSubject
                .doOnComplete(() -> {
                    activeSolvers.remove(input.getSolverId().toString());
                    DisposableUtil.disposeSubscriptions();
                })
                .subscribe(output -> latestOutputs.put(input.getSolverId().toString(), output));
        DisposableUtil.addDisposable(subscription);

        return solver.solve(input, (PublishSubject<TSPOutput>) progressSubject);
    }

    public TSPOutput getLatestOutput(String solverId) {
        return latestOutputs.get(solverId);
    }

    public Flowable<Event<TSPOutput>> streamProgress(String solverId) {
        PublishSubject<TSPOutput> progressSubject = activeSolvers.get(solverId);
        if (progressSubject == null) {
            return Flowable.empty();
        }
        return progressSubject.toFlowable(BackpressureStrategy.BUFFER).map(Event::of);
    }

    private double[][] processLines(List<String> lines) {
        // parse the data in the file
        String[] firstLine = lines.get(0).split("\\s+");
        int cityCount = Integer.parseInt(firstLine[0]);

        double[][] cities = new double[cityCount][2];
        for (int i = 1; i < cityCount + 1; i++) {
            String line = lines.get(i);
            String[] parts = line.split("\\s+");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            cities[i - 1] = new double[]{
                x,
                y,};
        }

        double[][] cityDistances = new double[cityCount][cityCount];
        for (int i = 0; i < cityCount; i++) {
            for (int j = 0; j < cityCount; j++) {
                if (i == j) {
                    continue;
                }
                double[] srcCity = cities[i];
                double[] destCity = cities[j];
                double distance = calculateCityDistance(srcCity[0], srcCity[1], destCity[0], destCity[1]);
                cityDistances[i][j] = distance;
                cityDistances[j][i] = distance;
            }
        }

        return cityDistances;
    }

    private double calculateCityDistance(double x1, double y1, double x2, double y2) {
        double xDelta = (x1 - x2);
        double yDelta = (y1 - y2);

        return Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
    }

}
