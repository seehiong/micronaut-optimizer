package io.github.seehiong.service;

import java.io.IOException;

import io.github.seehiong.model.input.Input;
import io.github.seehiong.model.output.Output;
import io.micronaut.http.multipart.CompletedFileUpload;
import reactor.core.publisher.Flux;

public interface SolverService<I extends Input, O extends Output> {

    I processInput(String input) throws IOException;

    I processFile(CompletedFileUpload file) throws IOException;

    Flux<Object> solve(I input) throws IOException;
}
