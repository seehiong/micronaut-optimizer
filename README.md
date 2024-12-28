# Micronaut Optimizer

## Rationale

This repository contains the Micronaut Optimizer project, which aims to optimize various computational problems using the Micronaut framework. The project leverages the power of Micronaut for building lightweight, modular, and efficient applications.

## Architecture

The architecture of the Micronaut Optimizer is visualized using PlantUML. Below is the updated PlantUML diagram representing the high-level architecture of the project.

```plantuml
@startuml
package "io.github.seehiong" {
    package "controller" {
        class FLPController
        class TSPController
    }
    
    package "factory" {
        class SolverFactory
    }
    
    package "service" {
        class FLPService
        class TSPService
    }
    
    package "utils" {
        class CoordUtils << (U,orchid) utility >>
        class FileUtils << (U,orchid) utility >>
    }

    package "model" {
        abstract class Input
        class FLPInput
        abstract class Output
        class FLPOutput
        interface Constraint
        interface Objective
        interface Metric
        interface Metadata
    }   

    package "solver" {
        interface Solver
        class FLPSolver
        class TSPSolver
    }    
}

FLPController --> FLPService
TSPController --> TSPService
FLPService --> SolverFactory
TSPService --> SolverFactory
SolverFactory --> Solver
TSPSolver --|> Solver
FLPSolver --|> Solver

Input <|-- FLPInput
Output <|-- FLPOutput
Input --> Constraint
Input --> Objective
Output --> Metric
Output --> Metadata

FLPSolver --> CoordUtils
FLPSolver --> FileUtils
@enduml
```

### Key Features

* Real-time Progress Monitoring: View optimization progress through SSE (Server-Sent Events)
* Multiple Solver Support:
  * Traveling Salesman Problem (TSP)
  * Facility Location Problem (FLP)
* Interactive Visualization: Dynamic visualization of solution progress
* Factory Pattern: Extensible solver framework

### Sample Progress Images

* FLP Progress

![FLP Progress](flp-progress.png)

* TSP Progress

![TSP Progress](tsp-progress.png)

## Folder Structure

The folder structure of the project is as follows:

```text
micronaut-optimizer/
├── src/main/
│   ├── java/io/github/seehiong/
│   │   ├── controller/   # REST endpoints
│   │   ├── factory/      # Solver factory implementation
│   │   ├── micronaut/    # Application configuration
│   │   ├── model/        # Data models and DTOs
│   │   ├── service/      # Business logic layer
│   │   ├── solver/       # Optimization algorithms
│   │   └── utils/        # Utility classes
│   └── resources/
│       ├── application.yml  # Micronaut configuration
│       └── logback.yml      # Logging configuration
```

## Prerequisites

* Java 21 or higher
* Gradle 8.11 or higher
* Web browser with SSE support

## Installation

1. Clone the repository:

```shell
git clone https://github.com/seehiong/micronaut-optimizer.git
cd micronaut-optimizer
```

2. Build the project:

```shell
./gradlew clean build
```

3. Run the application:

```shell
./gradlew run
```

4. Access the visualization:

* TSP Progress: http://localhost:8080/tsp-progress.html
* FLP Progress: http://localhost:8080/flp-progress.html

## API Endpoints

```text
POST /api/flp/uploadSolve    # Solve FLP problem
POST /api/tsp/uploadSolve    # Solve TSP problem
POST /api/tsp/solve          # Solve TSP problem
GET /api/flp/progress        # Stream FLP optimization progress
GET /api/tsp/progress        # Stream TSP optimization progress
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## License

MIT License