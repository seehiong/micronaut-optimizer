package io.github.seehiong.service;

import java.io.IOException;
import java.util.List;

import io.github.seehiong.controller.ProgressController;
import io.github.seehiong.model.constraint.DistanceMatrixConstraint;
import io.github.seehiong.model.input.TSPInput;
import io.github.seehiong.model.output.TSPOutput;
import io.github.seehiong.solver.Solver;
import io.github.seehiong.utils.DisposableUtil;
import io.github.seehiong.utils.FileUtil;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.serde.ObjectMapper;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import reactor.core.publisher.Flux;

public abstract class BaseTSPService extends BaseSolverService<TSPInput, TSPOutput> {

    protected final ObjectMapper objectMapper;
    protected final Solver<TSPInput, TSPOutput> solver;

    public BaseTSPService(ObjectMapper objectMapper, Solver<TSPInput, TSPOutput> solver) {
        this.objectMapper = objectMapper;
        this.solver = solver;
    }

    @Override
    public TSPInput processInput(String input) throws IOException {
        return objectMapper.readValue(input, TSPInput.class);
    }

    @Override
    public TSPInput processFile(CompletedFileUpload file) throws IOException {
        List<String> lines = FileUtil.readFile(file);
        return processLines(lines);
    }

    @Override
    public Flux<Object> solve(TSPInput input) {
        System.out.println("Solving TSP " + input.getSolverId());
        PublishSubject<TSPOutput> progressSubject = PublishSubject.create();
        ProgressController.activeSolvers.put(input.getSolverId().toString(), progressSubject);

        Disposable subscription = progressSubject
                .doOnComplete(() -> {
                    ProgressController.activeSolvers.remove(input.getSolverId().toString());
                    DisposableUtil.disposeSubscriptions();
                })
                .subscribe(output -> ProgressController.latestOutputs.put(input.getSolverId().toString(), output));
        DisposableUtil.addDisposable(subscription);

        return solver.solve(input, progressSubject);
    }

    protected TSPInput processLines(List<String> lines) {
        // Parse the data in the file
        String[] firstLine = lines.get(0).split("\\s+");
        int cityCount = Integer.parseInt(firstLine[0]);

        double[][] cities = new double[cityCount][2];
        for (int i = 1; i < cityCount + 1; i++) {
            String line = lines.get(i);
            String[] parts = line.split("\\s+");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            cities[i - 1] = new double[]{x, y};
        }

        double[][] cityDistances = new double[cityCount][cityCount];
        for (int i = 0; i < cityCount; i++) {
            for (int j = 0; j < cityCount; j++) {
                if (i == j) {
                    continue;
                }
                double[] srcCity = cities[i];
                double[] destCity = cities[j];
                double distance = calculateDistance(srcCity[0], srcCity[1], destCity[0], destCity[1]);
                cityDistances[i][j] = distance;
                cityDistances[j][i] = distance;
            }
        }

        return TSPInput.builder()
                .distanceMatrixConstraint(new DistanceMatrixConstraint(cityDistances))
                .build();
    }
}
