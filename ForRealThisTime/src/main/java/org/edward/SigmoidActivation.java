package org.edward;

public class SigmoidActivation implements ActivationFunction {
    @Override
    public double activate(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    @Override
    public double derivative(double x) {
        double sigmoid = activate(x);
        return sigmoid * (1 - sigmoid);
    }
}
