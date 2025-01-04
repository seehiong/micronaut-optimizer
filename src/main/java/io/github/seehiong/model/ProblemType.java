package io.github.seehiong.model;

public enum ProblemType {
    TSP, // Travelling Salesman Problem
    TSP_GA, // Travelling Salesman Problem with Genetic Algorithm
    FLP, // Facility Location Problem
    CVRP, // Capacitated Vehicle Routing Problem
    CVRP_MIP, // Capacitated Vehicle Routing Problem with Mixed Integer Programming
    BPP; // Bin Packing Problem

    public static ProblemType fromString(String problemType) {
        try {
            return ProblemType.valueOf(problemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Problem type not supported: " + problemType, e);
        }
    }

}
