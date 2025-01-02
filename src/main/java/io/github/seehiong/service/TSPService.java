package io.github.seehiong.service;

import io.github.seehiong.solver.TSPSolver;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("TSP")
public class TSPService extends BaseTSPService {

    public TSPService(ObjectMapper objectMapper) {
        super(objectMapper, new TSPSolver());
    }
}
