package org.edward;

public interface ActivationFunction {
    double activate(double x);

    double derivative(double x); // Derivative is needed for backpropagation
}

