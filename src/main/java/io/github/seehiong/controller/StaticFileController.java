package io.github.seehiong.controller;

import java.nio.file.Files;
import java.nio.file.Paths;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/")
public class StaticFileController {

    @Get("/tsp-progress.html")
    public HttpResponse<String> getTSPProgress() {
        return getProgress("src/main/resources/static/tsp-progress.html");
    }

    @Get("/flp-progress.html")
    public HttpResponse<String> getFLPProgress() {
        return getProgress("src/main/resources/static/flp-progress.html");
    }

    private HttpResponse<String> getProgress(String filename) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            return HttpResponse.ok(content).contentType("text/html");
        } catch (java.nio.file.NoSuchFileException e) {
            return HttpResponse.serverError("File not found");
        } catch (java.io.IOException e) {
            return HttpResponse.serverError("Error reading file");
        }
    }

}
