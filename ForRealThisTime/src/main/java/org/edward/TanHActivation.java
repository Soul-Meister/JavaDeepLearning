package org.edward;

public class TanHActivation implements ActivationFunction {
    @Override
    public double activate(double x) {
        return Math.tanh(x);
    }

    @Override
    public double derivative(double x) {
        double tanhX = Math.tanh(x);
        return 1.0 - tanhX * tanhX;
    }
}

