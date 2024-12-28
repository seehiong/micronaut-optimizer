package io.github.seehiong.solver;

import io.github.seehiong.model.input.Input;
import io.github.seehiong.model.output.Output;
import io.reactivex.rxjava3.subjects.PublishSubject;
import reactor.core.publisher.Flux;

public interface Solver<I extends Input, O extends Output> {

    Flux<Object> solve(I input, PublishSubject<O> progressSubject);

}
