package io.github.seehiong.solver;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.seehiong.model.SolverState;
import io.github.seehiong.model.input.TSPInput;
import io.github.seehiong.model.metadata.CitiesMetadata;
import io.github.seehiong.model.metric.CostMetric;
import io.github.seehiong.model.metric.TourMetric;
import io.github.seehiong.model.output.TSPOutput;
import io.github.seehiong.utils.CoordUtil;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
public class TSPGaSolver implements Solver<TSPInput, TSPOutput> {

    // Configuration
    final int POPULATION_SIZE = 100; // Population size
    final int MAX_GENERATIONS = 1200; // Maximum number of generations
    final double INITIAL_TEMPERATURE = 1000;
    final double FINAL_TEMPERATURE = 1;
    final double COOLING_RATE = 0.9995; // Cooling rate
    final int FITNESS_MEMO_SIZE = 10000000; // Limit the size of the fitness memo
    final int STAGNATION_THRESHOLD = 50; // Number of generations without improvement to trigger action
    final int LOCAL_SEARCH_ATTEMPTS = 50; // Number of attempts on local search before exiting local optima
    final int MUTATION_ATTEMPTS = 50; // Number of attempts on mutation
    int maxCities;
    double[][] graph;  // Populate with actual distances
    int maxStagnationRetry = 10;
    ConcurrentHashMap<Integer, Double> fitnessMemo = new ConcurrentHashMap<>(); // Thread-safe memoization for fitness
    int generation = 1;
    int optimalCount = 0;
    DecimalFormat df = new DecimalFormat("#");

    TSPOutput getIndividual(int[] genome) {
        return TSPOutput.builder()
                .tourMetric(new TourMetric(genome))
                .costMetric(new CostMetric(calculateFitness(genome)))
                .build();
    }

    TSPOutput getIndividual(int[] genome, double fitness) {
        return TSPOutput.builder()
                .tourMetric(new TourMetric(genome))
                .costMetric(new CostMetric(fitness))
                .build();
    }

    // Random number generator function
    int randNumber(int start, int end) {
        return (int) (Math.random() * (end - start)) + start;
    }

    double calculateDistance(int[] genome) {
        double distance = 0;
        for (int i = 0; i < genome.length - 1; i++) {
            int from = genome[i];
            int to = genome[i + 1];
            distance += graph[from][to];
        }

        //NOTE: This is distance needed to travel back to the original city
        distance += graph[genome[genome.length - 1]][0];
        return distance;
    }

    // Hash-based memoization for the fitness as total distance of the entire genome
    double calculateFitness(int[] genome) {
        int genomeHash = Arrays.hashCode(genome);
        if (fitnessMemo.containsKey(genomeHash)) {
            return fitnessMemo.get(genomeHash);
        }

        double totalDistance = calculateDistance(genome);
        if (fitnessMemo.size() < FITNESS_MEMO_SIZE) {
            fitnessMemo.put(genomeHash, totalDistance);
        }
        return totalDistance;
    }

    // Function to create a genome
    int[] createGenome() {
        int[] genome = new int[maxCities];
        genome[0] = 0;
        Set<Integer> newGenome = new HashSet<>();
        newGenome.add(0);

        int index = 1;
        while (newGenome.size() < maxCities) {
            int temp = randNumber(1, maxCities);
            if (!newGenome.contains(temp)) {
                genome[index++] = temp;
                newGenome.add(temp);
            }
        }
        return genome;
    }

    void reinitializePartOfPopulation(List<TSPOutput> population) {
        int reinitializeCount = POPULATION_SIZE / 5; // Reinitialize 20% of the population
        for (int i = 0; i < reinitializeCount; i++) {
            int[] genome = createGenome();
            population.set(randNumber(2, population.size()), getIndividual(genome)); // Replace random individuals (excluding the best two)
        }
    }

    List<TSPOutput> initialPopulation() {
        List<TSPOutput> population;
        population = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(getIndividual(createGenome()));
        }
        return population;
    }

    double calculateDelta(int[] genome, int i, int j) {
        // Calculate the difference in the tour length if the swap (i, j) is made
        return graph[genome[i - 1]][genome[j]] + graph[genome[i]][genome[j + 1]]
                - graph[genome[i - 1]][genome[i]] - graph[genome[j]][genome[j + 1]];
    }

    void twoOptSwap(int[] genome, int i, int j) {
        while (i < j) {
            int temp = genome[i];
            genome[i] = genome[j];
            genome[j] = temp;
            i++;
            j--;
        }
    }

    int[] localSearch(int[] genome) {
        int[] newGenome = genome.clone();
        boolean improvement = true;
        int iteration = 0;

        while (improvement && iteration < LOCAL_SEARCH_ATTEMPTS) {
            improvement = false;

            for (int i = 1; i < newGenome.length - 2; i++) {
                for (int j = i + 1; j < (newGenome.length - 1); j++) {
                    if (i == j) {
                        continue;
                    }

                    double delta = calculateDelta(newGenome, i, j);
                    if (delta < 0) {
                        twoOptSwap(newGenome, i, j);
                        improvement = true;
                        break; // Early exit on improvement
                    }
                }
                if (improvement) {
                    break; // Early exit on improvement

                }
            }
            iteration++;
        }
        return newGenome;
    }

    int[] inversionMutation(int[] genome) {
        int[] newGenome = genome.clone();
        int start = randNumber(1, maxCities);
        int end = randNumber(1, maxCities);

        while (start > end) {
            end = randNumber(1, maxCities);
        }

        while (start < end) {
            int temp = newGenome[start];
            newGenome[start] = newGenome[end];
            newGenome[end] = temp;
            start++;
            end--;
        }
        return newGenome;
    }

    int[] insertionMutation(int[] genome) {
        int[] newGenome = genome.clone();
        int start = randNumber(1, maxCities);
        int end = randNumber(1, maxCities);
        while (start == end) {
            end = randNumber(1, maxCities);
        }
        int temp = newGenome[start];
        if (start < end) {
            System.arraycopy(newGenome, start + 1, newGenome, start, end - start);
        } else {
            System.arraycopy(newGenome, end, newGenome, end + 1, start - end);
        }
        newGenome[end] = temp;
        return newGenome;
    }

    int[] swapMutation(int[] genome) {
        int[] newGenome = genome.clone();
        int index1 = randNumber(1, maxCities);  // Avoid swapping the first position
        int index2 = randNumber(1, maxCities);
        int temp = genome[index1];
        genome[index1] = genome[index2];
        genome[index2] = temp;
        return newGenome;
    }

    // Mutation Operator to maintain diversity in population
    int[] mutateGenome(int[] genome) {
        int[] newGenome = genome.clone();
        int mutationType = randNumber(0, 3);  // Adjust to the number of mutation operators
        return switch (mutationType) {
            case 0 ->
                inversionMutation(newGenome);
            case 1 ->
                insertionMutation(newGenome);
            default ->
                swapMutation(newGenome);
        };
    }

    int[] performMutation(TSPOutput individual, double temperature) {
        int[] newGenome = individual.getTourMetric().getTours().clone();
        double newFitness;
        int mutationAttempts = 0;

        while (mutationAttempts < MUTATION_ATTEMPTS) {
            mutationAttempts++;

            newGenome = mutateGenome(newGenome); // Apply mutation
            newGenome = localSearch(newGenome);  // Integrate local search
            newFitness = calculateFitness(newGenome);

            if (newFitness < individual.getCostMetric().getCost()
                    || Math.exp((individual.getCostMetric().getCost() - newFitness) / temperature) > Math.random()) {
                return newGenome;
            }
        }

        // No improvements after mutation attempts
        return individual.getTourMetric().getTours();
    }

    int[] performCrossover(int[] parent1, int[] parent2) {
        int length = parent1.length;
        int[] child = new int[length];
        Set<Integer> usedGenes = new HashSet<>();

        // Choose a segment from parent1
        int startPos = randNumber(1, length);
        int endPos = randNumber(1, length);

        // Ensure startPos is less than endPos
        if (startPos > endPos) {
            int temp = startPos;
            startPos = endPos;
            endPos = temp;
        }

        // Copy the segment from parent1 to the child
        for (int i = startPos; i < endPos; i++) {
            child[i] = parent1[i];
            usedGenes.add(parent1[i]);
        }

        // Ensure the first position is always the starting point
        child[0] = 0;
        usedGenes.add(0);

        // Fill the remaining positions with genes from parent2 in the order they appear
        int currentPos = endPos;
        for (int i = 1; i < length; i++) {
            int geneFromParent2 = parent2[i];
            if (!usedGenes.contains(geneFromParent2)) {
                usedGenes.add(geneFromParent2);
                if (currentPos >= length) {
                    currentPos = 1; // Wrap around to start filling from the beginning (skipping index 0)
                }
                if (child[currentPos] == 0) {
                    child[currentPos] = geneFromParent2;
                    currentPos++;
                }
            }
        }

        // Fill any remaining unfilled positions with random unused genes
        for (int i = 1; i < length; i++) {
            if (child[i] == 0) {
                int rand;
                do {
                    rand = randNumber(1, length);
                } while (usedGenes.contains(rand));
                child[i] = rand;
                usedGenes.add(rand);
            }
        }

        return child;
    }

    double adaptiveCooling(double temperature, int generation) {
        return temperature * Math.pow(COOLING_RATE, generation / (double) MAX_GENERATIONS);
    }

    // Combine simulated annealing with genetic algorithm
    void simulatedAnnealing(TSPOutput individual, double temperature) {
        for (int i = 0; i < 100; i++) {  // Simulated annealing iterations
            int[] mutatedGenome = mutateGenome(individual.getTourMetric().getTours());
            double mutatedFitness = calculateFitness(mutatedGenome);
            if (mutatedFitness < individual.getCostMetric().getCost() || Math.exp((individual.getCostMetric().getCost() - mutatedFitness) / temperature) > Math.random()) {
                individual.getTourMetric().setTours(mutatedGenome);
                individual.getCostMetric().setCost(mutatedFitness);
                break;
            }
        }
    }

    @Override
    public Flux<Object> solve(TSPInput input, PublishSubject<TSPOutput> progressSubject) {
        return Flux.create(emitter -> {

            log.info("starting solving for: {}", input.getSolverId());
            emitter.next(TSPOutput.builder()
                    .message(String.format("start solving for %s", input.getSolverId()))
                    .build());

            Instant startTime = Instant.now(); // Record the start time
            fitnessMemo.clear();
            graph = input.getDistances();
            maxCities = graph.length;

            List<TSPOutput> population = initialPopulation();
            TSPOutput bestIndividual = null;
            double bestFitness = Double.MAX_VALUE;
            int stagnationCount = 0;
            int stagnationResetCount = 0;
            double temperature = INITIAL_TEMPERATURE;

            CitiesMetadata cities = new CitiesMetadata(CoordUtil.deriveCoordinates(graph));

            while (temperature > FINAL_TEMPERATURE && generation < MAX_GENERATIONS && stagnationResetCount < maxStagnationRetry) {
                // Sort the population by fitness
                Collections.sort(population, Comparator.comparingDouble(a -> a.getCostMetric().getCost()));

                // Retain the best two individuals for elitism and crossover
                TSPOutput bestCurrentIndividual = population.get(0);
                TSPOutput secondBestIndividual = population.get(1);
                List<TSPOutput> newPopulation = Collections.synchronizedList(new ArrayList<>());
                newPopulation.add(bestCurrentIndividual);
                newPopulation.add(secondBestIndividual);

                // Perform crossover and mutation
                for (int i = 0; i < POPULATION_SIZE; i++) {
                    int[] newGnome;
                    if (Math.random() < 0.5) {
                        newGnome = performCrossover(bestCurrentIndividual.getTourMetric().getTours(), secondBestIndividual.getTourMetric().getTours());
                    } else {
                        TSPOutput randomIndividual = population.get(i);
                        newGnome = performMutation(randomIndividual, temperature);
                    }

                    newGnome = localSearch(newGnome);
                    TSPOutput newIndividual = getIndividual(newGnome, calculateFitness(newGnome));
                    simulatedAnnealing(newIndividual, temperature); // Apply simulated annealing
                    newPopulation.add(newIndividual);
                }

                // Ensure population is updated correctly
                if (!newPopulation.isEmpty()) {
                    // Use a mix of old and new population to maintain diversity
                    population = new ArrayList<>(newPopulation);
                    while (population.size() < POPULATION_SIZE) {
                        int[] genome = createGenome();
                        population.add(getIndividual(genome, calculateFitness(genome)));
                    }

                } else {
                    for (int i = 0; i < POPULATION_SIZE; i++) {
                        int[] genome = createGenome();
                        population.add(getIndividual(genome, calculateFitness(genome)));
                    }
                }

                // Update temperature regardless of population changes
                temperature = adaptiveCooling(temperature, generation);

                // Check for stagnation
                TSPOutput currentBest = Collections.min(population, Comparator.comparingDouble(a -> a.getCostMetric().getCost()));
                if (currentBest.getCostMetric().getCost() == bestFitness) {
                    optimalCount++;
                    log.info("potentially an optimal solution! {}", bestFitness);
                    if (optimalCount > 5) {
                        log.info("optimal solution, existing {}", bestFitness);
                        break;
                    }

                } else if (currentBest.getCostMetric().getCost() < bestFitness) {
                    optimalCount = 0;
                    bestIndividual = currentBest;
                    bestFitness = bestIndividual.getCostMetric().getCost();
                    Duration elapsedDuration = Duration.between(startTime, Instant.now());
                    log.debug("elapsed: {}, generation {}, fitness {}, fitnessMemo {}", elapsedDuration.toSeconds(), generation, df.format(bestFitness), fitnessMemo.size());
                    stagnationCount = 0;

                    // Publish progress update
                    if (progressSubject != null) {
                        progressSubject.onNext(TSPOutput.builder()
                                .solverState(SolverState.SOLVING)
                                .iteration(generation)
                                .tourMetric(bestIndividual.getTourMetric())
                                .costMetric(bestIndividual.getCostMetric())
                                .citiesMetadata(cities)
                                .build());
                    }
                    emitter.next(TSPOutput.builder()
                            .costMetric(bestIndividual.getCostMetric())
                            .iteration(generation)
                            .build());

                } else {
                    stagnationCount++;
                    if (stagnationCount >= STAGNATION_THRESHOLD) {
                        // Increase mutation rate to escape local optima
                        Duration elapsedDuration = Duration.between(startTime, Instant.now());
                        log.debug("elapsed: {}, stagnation detected: {}", elapsedDuration.toSeconds(), stagnationResetCount);

                        reinitializePartOfPopulation(population);  // Reinitialize part of the population
                        stagnationCount = 0;  // Reset stagnation count
                        stagnationResetCount++;
                    }
                }

                generation++;
            }

            // Find and print the most efficient path
            log.info("most efficient path after generations:{}, temperature: {}", generation, temperature);
            if (bestIndividual != null) {
                bestIndividual.setSolverId(input.getSolverId());
                bestIndividual.setIteration(generation);
                bestIndividual.setSolverState(SolverState.SOLVED);
                emitter.next(bestIndividual);

                if (progressSubject != null) {
                    bestIndividual.setCitiesMetadata(cities);
                    progressSubject.onNext(bestIndividual);
                }
            }

            emitter.complete();
            if (progressSubject != null) {
                progressSubject.onComplete();
            }

        }).subscribeOn(Schedulers.boundedElastic());
    }
}
