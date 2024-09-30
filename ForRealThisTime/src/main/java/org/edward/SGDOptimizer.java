package org.edward;

import java.util.List;

public class SGDOptimizer implements Optimizer {
    private double learningRate;
    private final double GRADIENT_CLIP_THRESHOLD = 1; // Define the gradient clipping threshold
    private HuberLoss lossFunction;

    public SGDOptimizer(double learningRate, double lambda) {
        this.learningRate = learningRate;
        this.lossFunction = new HuberLoss(1, 0.01);
    }

    public double getLearningRate() {
        return learningRate;
    }

    @Override
    public void updateWeights(List<Layer> layers, double[] inputs, double[] errors) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            double[] previousLayerOutputs = (i == 0) ? inputs : getLayerOutputs(layers.get(i - 1));

            for (int j = 0; j < layer.getNeurons().length; j++) {
                Neuron neuron = layer.getNeurons()[j];
                double delta = neuron.getDelta();

                // Clip gradients
                delta = clipGradient(delta);

                // Update weights
                for (int k = 0; k < neuron.getWeights().length; k++) {
                    double weightUpdate = learningRate * delta * previousLayerOutputs[k];
                    neuron.getWeights()[k] -= weightUpdate;
                }

                // Regularization: Apply L2 penalty to weights
                for (int k = 0; k < neuron.getWeights().length; k++) {
                    neuron.getWeights()[k] -= learningRate * lossFunction.getLambda() * neuron.getWeights()[k];
                }

                neuron.setBias(neuron.getBias() - learningRate * delta);
            }

            // Update next layer's deltas for backpropagation
            if (i > 0) {
                double[] nextLayerDeltas = computeNextLayerDeltas(layers.get(i - 1), layer);
                updateNeuronDeltas(layers.get(i - 1), nextLayerDeltas);
            }
        }
    }

    @Override
    public void update(List<Layer> layers, double[] inputs, double[] expectedOutputs) {
        double[] outputs = forwardPass(layers, inputs);

        // Compute the loss
        double totalLoss = 0.0;
        double[] outputErrors = new double[outputs.length];

        for (int i = 0; i < outputs.length; i++) {

            totalLoss += lossFunction.computeErrorWithRegularization(expectedOutputs[i], outputs[i], concatenateWeights(layers));
            outputErrors[i] = lossFunction.derivative(expectedOutputs[i], outputs[i]);
        }

        backpropagateAndUpdateWeights(layers, inputs, outputErrors);
    }

    private double[] forwardPass(List<Layer> layers, double[] inputs) {
        double[] currentInputs = inputs;

        for (Layer layer : layers) {
            double[] newInputs = new double[layer.getNeurons().length];
            for (int j = 0; j < layer.getNeurons().length; j++) {
                Neuron neuron = layer.getNeurons()[j];
                newInputs[j] = neuron.activate(currentInputs, neuron.getActivationFunction());
            }
            currentInputs = newInputs;
        }
        return currentInputs;
    }

    private double[] computeOutputLayerErrors(double[] outputs, double[] expectedOutputs) {
        double[] errors = new double[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            errors[i] = outputs[i] - expectedOutputs[i];
        }
        return errors;
    }

    private void backpropagateAndUpdateWeights(List<Layer> layers, double[] inputs, double[] outputErrors) {
        updateNeuronDeltas(layers.get(layers.size() - 1), outputErrors);
        updateWeights(layers, inputs, outputErrors);
    }

    private double[] getLayerOutputs(Layer layer) {
        double[] outputs = new double[layer.getNeurons().length];
        for (int i = 0; i < layer.getNeurons().length; i++) {
            outputs[i] = layer.getNeurons()[i].getOutput();
        }
        return outputs;
    }

    private double[] computeNextLayerDeltas(Layer previousLayer, Layer currentLayer) {
        double[] nextLayerDeltas = new double[previousLayer.getNeurons().length];

        for (int i = 0; i < previousLayer.getNeurons().length; i++) {
            double deltaSum = 0;
            for (int j = 0; j < currentLayer.getNeurons().length; j++) {
                Neuron neuron = currentLayer.getNeurons()[j];
                deltaSum += neuron.getDelta() * neuron.getWeights()[i];
            }
            nextLayerDeltas[i] = deltaSum;
        }

        return nextLayerDeltas;
    }

    private void updateNeuronDeltas(Layer layer, double[] deltas) {
        for (int i = 0; i < layer.getNeurons().length; i++) {
            layer.getNeurons()[i].setDelta(deltas[i]);
        }
    }

    private double clipGradient(double gradient) {
        if (Math.abs(gradient) > GRADIENT_CLIP_THRESHOLD) {
            return Math.signum(gradient) * GRADIENT_CLIP_THRESHOLD;
        }
        return gradient;
    }

    private double[] concatenateWeights(List<Layer> layers) {
        int totalLength = 0;
        for (Layer layer : layers) {
            for (Neuron neuron : layer.getNeurons()) {
                totalLength += neuron.getWeights().length;
            }
        }

        double[] concatenatedWeights = new double[totalLength];
        int currentIndex = 0;

        for (Layer layer : layers) {
            for (Neuron neuron : layer.getNeurons()) {
                System.arraycopy(neuron.getWeights(), 0, concatenatedWeights, currentIndex, neuron.getWeights().length);
                currentIndex += neuron.getWeights().length;
            }
        }

        return concatenatedWeights;
    }
}
