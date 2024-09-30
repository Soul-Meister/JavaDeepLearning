package org.edward;

import java.util.Random;


public class Neuron {
    private double[] weights;
    private double bias;
    private double output;
    private double delta; // For backpropagation
    private double[] inputs;
    private ActivationFunction activationFunction = new LeakyReLUActivation(0.01);

    public ActivationFunction getActivationFunction() {
        return activationFunction;
    }

    public Neuron(int numInputs) {
        Random rand = new Random();
        weights = new double[numInputs];
        double stddev = Math.sqrt(1.0 / numInputs); // Xavier initialization standard deviation
        for (int i = 0; i < numInputs; i++) {
            weights[i] = rand.nextGaussian() * stddev; // Initialize weights using Xavier initialization
        }
        bias = rand.nextGaussian() * stddev; // Initialize bias using the same standard deviation
    }

    public double activate(double[] inputs, ActivationFunction activationFunction) {
        this.inputs = inputs;
        double sum = bias;
        for (int i = 0; i < inputs.length; i++) {
            sum += weights[i] * inputs[i];
        }
        output = activationFunction.activate(sum);
        return output;
    }


    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getOutput() {
        return output;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public double getInput(int i) {
        return inputs[i];
    }
}
