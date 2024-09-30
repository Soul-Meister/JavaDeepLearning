package org.edward;




public class FlappyBirdEnvironment {

    static Drawing drawing = new Drawing();
    public static int temp = -1;

    public FlappyBirdEnvironment() {

        drawing.run();
    }

    public double[] getData(){
       return normalize(drawing.getData(), -1000, 1000);
    }

    public double[] normalize(double[] data, double min, double max) {
        double[] normalizedData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            normalizedData[i] = (data[i] - min) / (max - min);
        }
        return normalizedData;
    }


    public boolean isRunning(){
        return drawing.getTimer();
    }
    public void updateState(){
        if(!isRunning()){
            restart();
        }
    }
    public void restart(){
        drawing.restart();
        startTimer();
    }

    public void startTimer(){
        drawing.startTimer();
    }

    public int getScore(){
        return Player.score;
    }

    public int performAction(double actionVal){
        if(Drawing.generations % 25 == 0){
            if(temp != Drawing.generations){
                System.out.println("\rGenerations: " + Drawing.generations + "   Max Score: " + Drawing.maxScore);
                temp = Drawing.generations;
            }
        }
        if(actionVal == 0) { // Lowered the jump threshold to encourage more jumps
            Player.jump(Drawing.jumpParts);
        }
        return drawing.getReward();
    }

}
