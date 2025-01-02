package io.github.seehiong.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.seehiong.model.ProblemType;
import io.github.seehiong.service.SolverService;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;

@Factory
public class SolverServiceFactory {

    @Bean
    public Map<ProblemType, SolverService<?, ?>> services(List<SolverService<?, ?>> solverServices) {
        Map<ProblemType, SolverService<?, ?>> services = new HashMap<>();
        for (SolverService<?, ?> service : solverServices) {
            String qualifier = service.getClass().getAnnotation(Named.class).value();
            ProblemType problemType = ProblemType.valueOf(qualifier);
            services.put(problemType, service);
        }
        return services;
    }

}
