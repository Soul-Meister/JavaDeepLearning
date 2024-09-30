package org.edward;

public class LeakyReLUActivation implements ActivationFunction {
    private final double alpha; // Small positive slope for the negative side

    // Constructor to set the alpha value
    public LeakyReLUActivation(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public double activate(double x) {
        return x > 0 ? x : alpha * x;
    }

    @Override
    public double derivative(double x) {
        return x > 0 ? 1 : alpha;
    }
}
