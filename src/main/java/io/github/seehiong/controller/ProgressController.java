package io.github.seehiong.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.seehiong.model.output.Output;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.sse.Event;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.RequiredArgsConstructor;

@Controller("/progress")
@RequiredArgsConstructor
public class ProgressController {

    public static final Map<String, PublishSubject<? extends Output>> activeSolvers = new ConcurrentHashMap<>();
    public static final Map<String, Output> latestOutputs = new ConcurrentHashMap<>();

    @Get("/latest/{solverId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Output getLatestOutput(@PathVariable String solverId) {
        return latestOutputs.get(solverId);
    }

    @Get(value = "/{solverId}", produces = MediaType.TEXT_EVENT_STREAM)
    public Flowable<Event<? extends Output>> streamProgress(String solverId) {
        PublishSubject<? extends Output> progressSubject = activeSolvers.get(solverId);
        if (progressSubject == null) {
            return Flowable.empty();
        }
        return progressSubject.toFlowable(BackpressureStrategy.BUFFER).map(Event::of);
    }
}
