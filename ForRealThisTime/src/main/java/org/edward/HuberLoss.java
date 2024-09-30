package org.edward;

public class HuberLoss implements LossFunction {
    private double delta; // Huber loss threshold
    private double lambda; // Regularization strength

    public HuberLoss(double delta, double lambda) {
        this.delta = delta; // Threshold for quadratic/linear transition
        this.lambda = lambda; // L2 regularization strength
    }

    @Override
    public double computeError(double target, double output) {
        double a = target - output;
        if (Math.abs(a) <= delta) {
            // Quadratic loss
            return 0.5 * a * a;
        } else {
            // Linear loss
            return delta * (Math.abs(a) - 0.5 * delta);
        }
    }

    public double computeErrorWithRegularization(double target, double output, double[] weights) {
        // Calculate Huber loss
        double huberLoss = computeError(target, output);

        // L2 Regularization: 0.5 * lambda * sum(w_i^2)
        double l2Regularization = 0.5 * lambda * sumOfSquares(weights);

        // Total loss: Huber loss + L2 regularization
        return huberLoss + l2Regularization;
    }

    @Override
    public double derivative(double target, double output) {
        double a = target - output;
        if (Math.abs(a) <= delta) {
            // Quadratic derivative
            return -a;
        } else {
            // Linear derivative
            return -delta * Math.signum(a);
        }
    }

    @Override
    public double getLambda() {
        return lambda;
    }

    // Helper function to calculate the sum of squares of the weights (for L2 regularization)
    private double sumOfSquares(double[] weights) {
        double sum = 0.0;
        for (double weight : weights) {
            sum += weight * weight;
        }
        return sum;
    }
}
