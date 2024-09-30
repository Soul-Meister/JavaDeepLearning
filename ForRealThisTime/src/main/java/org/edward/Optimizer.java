package org.edward;

import java.util.List;

public interface Optimizer {
    void updateWeights(List<Layer> layers, double[] inputs, double[] errors);
    void update(List<Layer> layers, double[] inputs, double[] expectedOutputs);
    double getLearningRate();

}


