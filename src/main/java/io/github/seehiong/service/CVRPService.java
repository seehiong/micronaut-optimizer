package io.github.seehiong.service;

import io.github.seehiong.service.base.BaseCVRPService;
import io.github.seehiong.solver.CVRPSolver;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("CVRP")
public class CVRPService extends BaseCVRPService {

    public CVRPService(ObjectMapper objectMapper) {
        super(objectMapper, new CVRPSolver());
    }
}
