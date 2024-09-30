package org.edward;

public class ReinforcementLearning {
    private final NeuralNetwork nn;
    private final EpsilonGreedy policy;
    private final Optimizer optimizer; // Reference to optimizer
    private double gamma = 0.94; // Discount factor for future rewards
    private HuberLoss huberLoss = new HuberLoss(1, .01);
    private double learningRate = 0.015;
    private ReplayBuffer replayBuffer = new ReplayBuffer(5000, 0.7); // Adjust the buffer size as needed
    private int batchSize = 64; // Define the size of the training batch




    public ReinforcementLearning() {
        optimizer = new AdamOptimizer(learningRate);
        nn = new NeuralNetwork(new LeakyReLUActivation(0.01), huberLoss, optimizer);
        nn.addLayer(12, 4); // First hidden layer with 12 neurons and 2 inputs
        nn.addLayer(12, 12);
        nn.addLayer(16, 12);// second hidden layer with 12x8 neurons
        nn.addLayer(2, 16); // Output layer with 2 neuron (Q-value) and 8 inputs from the previous layer

        policy = new EpsilonGreedy(0.7, 0.9999, 0.0005); // Epsilon decay over time
    }


    public void trainInRealTime(FlappyBirdEnvironment game) {
        while (game.isRunning()) {
            // Get the current state and predict Q-values
            double[] state = game.getData();
            double[] qValues = nn.predict(state);

            // Choose an action based on the epsilon-greedy policy
            int action = policy.selectAction(qValues);

            // Take action in the environment and get the reward
            double reward = game.performAction(action);

            // Get the new state after the action
            double[] nextState = game.getData();

            double[] nextQValues = nn.predict(nextState);
            double targetQValue = reward + gamma * getMaxValue(nextQValues);
            double tdError = Math.abs(targetQValue - qValues[action]); // Use TD error as priority

            // Add experience with TD error as priority
            replayBuffer.addExperience(new Experience(state, action, reward, nextState, tdError), game.getScore());

            // If the buffer has enough samples, train the network using a batch
            if (replayBuffer.isReady(batchSize)) {
                Experience[] batch = replayBuffer.getBatch(batchSize);

                for (Experience exp : batch) {
                    nextQValues = nn.predict(exp.nextState);
                    targetQValue = exp.reward + gamma * getMaxValue(nextQValues);

                    // Predict Q-values for the current state
                    double[] updatedQValues = nn.predict(exp.state);

                    // Apply the Bellman update to the Q-values
                    updatedQValues[exp.action] += optimizer.getLearningRate() * (targetQValue - updatedQValues[exp.action]);

                    // Update the neural network with the updated Q-values
                    optimizer.update(nn.getLayers(), exp.state, updatedQValues);
                }
            }

            System.out.print("\rEpsilon: " + policy.getEpsilon() + "  qValue[0]: " + qValues[0] + "  qValues[1]: " + qValues[1] + "  Reward: " + reward);
            // Decay epsilon to reduce exploration over time
            policy.decayEpsilon();

            // Update the game state (restart if game is over)
            game.updateState();

            // Sleep to control the speed of the training loop
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private double getMaxValue(double[] values) {
        double max = values[0];
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static void main(String[] args) {
        ReinforcementLearning flappyBirdRL = new ReinforcementLearning();
        FlappyBirdEnvironment game = new FlappyBirdEnvironment(); // Assume this is your Flappy Bird game class
        flappyBirdRL.trainInRealTime(game);
    }
}


