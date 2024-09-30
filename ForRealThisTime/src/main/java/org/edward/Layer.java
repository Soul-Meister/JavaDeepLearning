package org.edward;

public class Layer {
    private Neuron[] neurons;

    public Layer(int numNeurons, int numInputsPerNeuron) {
        neurons = new Neuron[numNeurons];
        for (int i = 0; i < numNeurons; i++) {
            neurons[i] = new Neuron(numInputsPerNeuron);
        }
    }

    public double[] feedForward(double[] inputs, ActivationFunction activationFunction) {
        double[] outputs = new double[neurons.length];
        for (int i = 0; i < neurons.length; i++) {
            outputs[i] = neurons[i].activate(inputs, activationFunction);
        }
        return outputs;
    }

    public Neuron[] getNeurons() {
        return neurons;
    }
}

