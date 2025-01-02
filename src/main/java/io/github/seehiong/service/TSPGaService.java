package io.github.seehiong.service;

import io.github.seehiong.solver.TSPGaSolver;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("TSP_GA")
public class TSPGaService extends BaseTSPService {

    public TSPGaService(ObjectMapper objectMapper) {
        super(objectMapper, new TSPGaSolver());
    }
}
