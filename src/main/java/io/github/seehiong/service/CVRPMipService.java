package io.github.seehiong.service;

import io.github.seehiong.service.base.BaseCVRPService;
import io.github.seehiong.solver.CVRPMipSolver;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("CVRP_MIP")
public class CVRPMipService extends BaseCVRPService {

    public CVRPMipService(ObjectMapper objectMapper) {
        super(objectMapper, new CVRPMipSolver());
    }
}
