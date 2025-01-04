package io.github.seehiong.service;

import java.io.IOException;

import io.github.seehiong.controller.ProgressController;
import io.github.seehiong.model.input.BPPInput;
import io.github.seehiong.model.output.BPPOutput;
import io.github.seehiong.service.base.BaseSolverService;
import io.github.seehiong.solver.BPPSolver;
import io.github.seehiong.solver.base.Solver;
import io.github.seehiong.utils.DisposableUtil;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.serde.ObjectMapper;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Singleton
@Named("BPP")
public class BPPService extends BaseSolverService<BPPInput, BPPOutput> {

    private final ObjectMapper objectMapper;
    private final Solver<BPPInput, BPPOutput> solver;

    public BPPService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.solver = new BPPSolver();
    }

    @Override
    public BPPInput processInput(String input) throws IOException {
        log.debug("Processing input: {}", input);
        return objectMapper.readValue(input, BPPInput.class);
    }

    @Override
    public BPPInput processFile(CompletedFileUpload file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Flux<Object> solve(BPPInput input) throws IOException {
        PublishSubject<BPPOutput> progressSubject = PublishSubject.create();
        ProgressController.activeSolvers.put(input.getSolverId().toString(), progressSubject);

        Disposable subscription;
        subscription = progressSubject
                .doOnComplete(() -> {
                    ProgressController.activeSolvers.remove(input.getSolverId().toString());
                    DisposableUtil.disposeSubscriptions();
                })
                .subscribe(output -> ProgressController.latestOutputs.put(input.getSolverId().toString(), output));
        DisposableUtil.addDisposable(subscription);

        return solver.solve(input, (PublishSubject<BPPOutput>) progressSubject);
    }
}
