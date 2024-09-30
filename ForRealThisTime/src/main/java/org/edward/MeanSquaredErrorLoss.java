package org.edward;

public class MeanSquaredErrorLoss implements LossFunction {
    private double lambda; // Regularization strength
    public static double mseCalculated;

    // Constructor to set the regularization strength
    public MeanSquaredErrorLoss(double lambda) {
        this.lambda = lambda;
    }
    @Override
    public double getLambda(){
        return lambda;
    }

    @Override
    public double computeError(double target, double output) {
        return 0.5 * Math.pow(target - output, 2);
    }

    public double computeErrorWithRegularization(double target, double output, double[] weights) {
        // MSE Loss
        double mse = computeError(target, output);

        // L2 Regularization term: 0.5 * lambda * sum(w_i^2)
        double l2Regularization = 0.5 * lambda * sumOfSquares(weights);

        // Total loss = MSE + L2 Regularization
        mseCalculated = mse + l2Regularization;
        return mse + l2Regularization;
    }

    public double getMSE(){
        return mseCalculated;
    }

    @Override
    public double derivative(double target, double output) {
        return output - target; // Derivative of MSE with respect to output
    }

    private double sumOfSquares(double[] weights) {
        double sum = 0.0;
        for (double weight : weights) {
            sum += Math.pow(weight, 2);
        }
        return sum;
    }
}
