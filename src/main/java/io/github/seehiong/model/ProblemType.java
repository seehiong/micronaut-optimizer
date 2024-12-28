package io.github.seehiong.model;

public enum ProblemType {
    TSP,
    TSP_GA,
    FLP;

    public static ProblemType fromString(String problemType) {
        try {
            return ProblemType.valueOf(problemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid problemType type: " + problemType, e);
        }
    }

}
