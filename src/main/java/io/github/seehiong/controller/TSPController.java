package io.github.seehiong.controller;

import java.io.IOException;

import io.github.seehiong.model.input.TSPInput;
import io.github.seehiong.model.output.TSPOutput;
import io.github.seehiong.service.TSPService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.sse.Event;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Controller("/tsp")
@RequiredArgsConstructor
public class TSPController {

    private final TSPService tspService;

    @Post(value = "/solve", produces = MediaType.TEXT_EVENT_STREAM)
    public Flux<Object> solve(@Body TSPInput input) {
        return tspService.solve(input);
    }

    @Post(value = "/uploadSolve", produces = MediaType.TEXT_EVENT_STREAM, consumes = MediaType.MULTIPART_FORM_DATA)
    public Flux<Object> uploadSolve(@Body TSPInput input, CompletedFileUpload file) throws IOException {
        return tspService.uploadSolve(input, file);
    }

    @Get("/latest/{solverId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TSPOutput getLatestOutput(@PathVariable String solverId) {
        return tspService.getLatestOutput(solverId);
    }

    @Get(value = "/progress/{solverId}", produces = MediaType.TEXT_EVENT_STREAM)
    public Flowable<Event<TSPOutput>> streamProgress(String solverId) {
        return tspService.streamProgress(solverId);
    }

}
