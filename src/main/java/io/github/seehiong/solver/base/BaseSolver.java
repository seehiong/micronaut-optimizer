package io.github.seehiong.solver.base;

import io.github.seehiong.model.SolverState;
import io.github.seehiong.model.input.Input;
import io.github.seehiong.model.output.Output;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

@Slf4j
@Singleton
public abstract class BaseSolver<I extends Input, O extends Output> implements Solver<I, O> {

    protected abstract O createOutput();

    protected O startSolve(I input) {
        log.info("start solving for {}", input.getSolverId());

        O output = createOutput();
        output.setMessage(String.format("start solving for %s", input.getSolverId()));
        output.setSolverState(SolverState.SOLVING);
        return output;
    }

    protected void publishNext(FluxSink<Object> emitter, PublishSubject<O> publisher, O output) {
        emitter.next(output);
        publisher.onNext(output);
    }

    protected void publishComplete(FluxSink<Object> emitter, PublishSubject<O> publisher) {
        emitter.next("complete");
        emitter.complete();
        publisher.onComplete();
    }
}
