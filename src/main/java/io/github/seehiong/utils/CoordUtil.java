package io.github.seehiong.utils;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CoordUtil {

    public double[][] deriveCoordinates(double[][] distances) {
        int n = distances.length;
        double[][] coordinates = new double[n][2];

        // Create the distance matrix
        RealMatrix distanceMatrix = new Array2DRowRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distanceMatrix.setEntry(i, j, distances[i][j]);
            }
        }

        // Double centering
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(n);
        RealMatrix ones = new Array2DRowRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                ones.setEntry(i, j, 1.0 / n);
            }
        }
        RealMatrix centeringMatrix = identity.subtract(ones);
        RealMatrix B = centeringMatrix.multiply(distanceMatrix).multiply(centeringMatrix).scalarMultiply(-0.5);

        // Eigen decomposition
        EigenDecomposition eigenDecomposition = new EigenDecomposition(B);
        double[] eigenValues = eigenDecomposition.getRealEigenvalues();
        RealMatrix eigenVectors = eigenDecomposition.getV();

        // Select the top 2 eigenvalues and corresponding eigenvectors
        double[] topEigenValues = {eigenValues[0], eigenValues[1]};
        RealMatrix topEigenVectors = eigenVectors.getSubMatrix(0, n - 1, 0, 1);

        // Compute the coordinates
        for (int i = 0; i < n; i++) {
            coordinates[i][0] = topEigenVectors.getEntry(i, 0) * Math.sqrt(topEigenValues[0]);
            coordinates[i][1] = topEigenVectors.getEntry(i, 1) * Math.sqrt(topEigenValues[1]);
        }

        return coordinates;
    }

}
