package org.edward;

import java.util.List;

public class AdamOptimizer implements Optimizer {
    private double learningRate;
    private final double beta1 = 0.9;
    private final double beta2 = 0.999;
    private final double epsilon = 1e-8;
    private final double GRADIENT_CLIP_THRESHOLD = 3;

    // These variables store the moving averages of gradients and squared gradients for each neuron
    private double[][] m;
    private double[][] v;
    private int t = 0; // Time step

    public AdamOptimizer(double learningRate) {
        this.learningRate = learningRate;
    }

    @Override
    public void updateWeights(List<Layer> layers, double[] inputs, double[] errors) {
        t++; // Increment the time step
        initializeAdam(layers);

        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            double[] previousLayerOutputs = (i == 0) ? inputs : getLayerOutputs(layers.get(i - 1));

            for (int j = 0; j < layer.getNeurons().length; j++) {
                Neuron neuron = layer.getNeurons()[j];
                double delta = neuron.getDelta();

                // Clip gradients
                //delta = clipGradient(delta);

                for (int k = 0; k < neuron.getWeights().length; k++) {
                    double grad = delta * previousLayerOutputs[k];

                    // Update biased first moment estimate (m)
                    m[i][k] = beta1 * m[i][k] + (1 - beta1) * grad;

                    // Update biased second moment estimate (v)
                    v[i][k] = beta2 * v[i][k] + (1 - beta2) * grad * grad;

                    // Bias-corrected estimates
                    double mHat = m[i][k] / (1 - Math.pow(beta1, t));
                    double vHat = v[i][k] / (1 - Math.pow(beta2, t));

                    // Update weight using Adam update rule
                    neuron.getWeights()[k] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
                }

                // Update bias
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

    }

    @Override
    public double getLearningRate() {
        return 0;
    }

    // Initialize the Adam-specific moving averages for all layers if they haven't been initialized
    private void initializeAdam(List<Layer> layers) {
        if (m == null || v == null) {
            m = new double[layers.size()][];
            v = new double[layers.size()][];

            for (int i = 0; i < layers.size(); i++) {
                int numWeights = layers.get(i).getNeurons()[0].getWeights().length;
                m[i] = new double[numWeights];
                v[i] = new double[numWeights];
            }
        }
    }

    private double[] getLayerOutputs(Layer layer) {
        double[] outputs = new double[layer.getNeurons().length];
        for (int i = 0; i < layer.getNeurons().length; i++) {
            outputs[i] = layer.getNeurons()[i].getOutput();
        }
        return outputs;
    }

    private double clipGradient(double gradient) {
        if (Math.abs(gradient) > GRADIENT_CLIP_THRESHOLD) {
            return Math.signum(gradient) * GRADIENT_CLIP_THRESHOLD;
        }
        return gradient;
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
}
