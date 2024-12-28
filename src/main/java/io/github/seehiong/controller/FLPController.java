package io.github.seehiong.controller;

import java.io.IOException;

import io.github.seehiong.model.input.FLPInput;
import io.github.seehiong.model.output.FLPOutput;
import io.github.seehiong.service.FLPService;
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

@Controller("/flp")
@RequiredArgsConstructor
public class FLPController {

    private final FLPService flpService;

    @Post(value = "/uploadSolve", produces = MediaType.TEXT_EVENT_STREAM, consumes = MediaType.MULTIPART_FORM_DATA)
    public Flux<Object> uploadSolve(@Body FLPInput input, CompletedFileUpload file) throws IOException {
        return flpService.uploadSolve(input, file);
    }

    @Get("/latest/{solverId}")
    @Produces(MediaType.APPLICATION_JSON)
    public FLPOutput getLatestOutput(@PathVariable String solverId) {
        return flpService.getLatestOutput(solverId);
    }

    @Get(value = "/progress/{solverId}", produces = MediaType.TEXT_EVENT_STREAM)
    public Flowable<Event<FLPOutput>> streamProgress(String solverId) {
        return flpService.streamProgress(solverId);
    }

}
