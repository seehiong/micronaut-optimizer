package io.github.seehiong.controller;

import java.io.IOException;
import java.util.Map;

import io.github.seehiong.model.ProblemType;
import io.github.seehiong.model.input.Input;
import io.github.seehiong.model.output.Output;
import io.github.seehiong.service.SolverService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Controller("/solve")
@RequiredArgsConstructor
public class ProblemController {

    private final Map<ProblemType, SolverService<?, ?>> services;

    @Post(value = "/{problem}", produces = MediaType.TEXT_EVENT_STREAM)
    public Flux<Object> solve(@Body String rawInput, @PathVariable String problem) {
        try {
            SolverService<?, ?> service = getService(problem);
            Input input = (Input) service.processInput(rawInput);
            return ((SolverService<Input, Output>) service).solve(input);

        } catch (IOException e) {
            return Flux.error(new RuntimeException("Failed to deserialize input", e));
        }
    }

    @Post(value = "/{problem}/upload", produces = MediaType.TEXT_EVENT_STREAM, consumes = MediaType.MULTIPART_FORM_DATA)
    public Flux<Object> uploadSolve(CompletedFileUpload file, @PathVariable String problem) throws IOException {
        try {
            SolverService<?, ?> service = getService(problem);
            Input input = (Input) service.processFile(file);
            return ((SolverService<Input, Output>) service).solve(input);

        } catch (IOException e) {
            return Flux.error(new RuntimeException("Failed to deserialize input", e));
        }
    }

    private SolverService<?, ?> getService(String problem) {
        ProblemType problemType = ProblemType.fromString(problem.toUpperCase());
        SolverService<?, ?> service = services.get(problemType);

        if (service == null) {
            throw new IllegalArgumentException("Problem not supported");
        }
        return service;
    }
}
