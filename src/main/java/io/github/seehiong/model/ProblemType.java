package io.github.seehiong.model;

public enum ProblemType {
    TSP,
    TSP_GA,
    FLP,
    CVRP;

    public static ProblemType fromString(String problemType) {
        try {
            return ProblemType.valueOf(problemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Problem type not supported: " + problemType, e);
        }
    }

}
