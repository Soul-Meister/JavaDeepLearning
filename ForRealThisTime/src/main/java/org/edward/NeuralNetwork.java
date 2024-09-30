package org.edward;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {
    private List<Layer> layers;
    private ActivationFunction activationFunction;
    private LossFunction lossFunction;
    private Optimizer optimizer;

    public NeuralNetwork(ActivationFunction activationFunction, LossFunction lossFunction, Optimizer optimizer) {
        this.layers = new ArrayList<>();
        this.activationFunction = activationFunction;
        this.lossFunction = lossFunction;
        this.optimizer = optimizer;
    }

    public void addLayer(int numNeurons, int numInputsPerNeuron) {
        Layer layer = new Layer(numNeurons, numInputsPerNeuron);
        layers.add(layer);
    }

    public double[] predict(double[] inputs) {
        double[] outputs = inputs;
        for (Layer layer : layers) {
            outputs = layer.feedForward(outputs, activationFunction);
        }
        return outputs;
    }

    public List<Layer> getLayers() {
        return layers;
    }
}
