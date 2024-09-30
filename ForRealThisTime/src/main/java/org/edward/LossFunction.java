package org.edward;

public interface LossFunction {
    double computeError(double target, double output);
    double derivative(double target, double output);
    double getLambda();
}

