package org.edward;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.Font; 
import java.awt.Rectangle;
import java.util.concurrent.CopyOnWriteArrayList;

public class Drawing extends Canvas {
    public static int generations = 0;
    public static int space = 150;
    public static int tickCount = 0;
    private int point;
    private int period = 10;
    private int width = 50; // Adjust wall width
    private int height = 15000; // Adjust wall height
    public static List<Particles> jumpParts = new CopyOnWriteArrayList<>();
    public static List<Wall> walls = new CopyOnWriteArrayList<>();
    public static List<LowerWall> lowerwalls = new CopyOnWriteArrayList<>();
    private Timer timer;
    public static int level; 
    public static boolean restart = false;
    public static int maxScore = 0;

    private boolean restarted = false;
    public int tempScore = 0;


    public void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Generation: " + generations, 20, 30);
        g.drawString("Score: " + Player.score, 20, 80 );// Adjust the position as needed
        g.drawString("Level: ", 20, -800);
    }

    public Drawing() {
        setFocusable(true);

        // Add a key listener to detect space key presses
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    Player.jump(jumpParts); // Call the jump method when space key is pressed
                }
                if(e.getKeyCode() == KeyEvent.VK_R) {
                    restart();
                }
            }
        });

        timer = new Timer(12, new ActionListener() { // 7ms for 140 fps
            public void actionPerformed(ActionEvent e) {
                update();
                repaint(); // Request a repaint
            }
        });
        timer.start();

    }

    public boolean checkCollisions() {
        Rectangle floorBounds = Floor.getFloorBoundingBox(this);
        Rectangle playerBounds = Player.getPlayerBoundingBox();

        if(playerBounds.intersects(floorBounds)) {
            //timer.stop();

            //restart = true;

            //restart();
           // return true;
        }
    
        // Check collisions with walls
        for (Wall wall : walls) {
            Rectangle wallBounds = wall.getWallBoundingBox();
            if (playerBounds.intersects(wallBounds)) {
              //timer.stop();
              //restart = true;
              restart();

              return true;
            }
        }
        for (LowerWall lowerWall : lowerwalls) {
            Rectangle lowerWallBounds = lowerWall.getWallBoundingBox();
            if (playerBounds.intersects(lowerWallBounds)) {
                //timer.stop();
               // restart = true;
             restart();

             return true;
            }
        }
        return false;
    }

    public void run(){
        JFrame frame = new JFrame("Flappy Bird");
        Canvas canvas = new Drawing();
        frame.getContentPane().setBackground(Color.black);
        canvas.setSize(1000, 1000);
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setFocusable(true);
    }

    public int getNextWallDistance() {
        int playerX = Player.px;
        int closestDistance = 800;
        
        for (Wall wall : walls) {
            int wallX = wall.getX();
            if (wallX > playerX) {
                int distance = wallX - playerX;
                if (distance < closestDistance) {
                    closestDistance = distance;
                }
            }
        }
        
        return closestDistance;
    }

    public int getPointForClosestWall() {
        int playerX = Player.px;
        int closestPoint = -1;
        
        for (Wall wall : walls) {
            int wallX = wall.getX();
            if (wallX > playerX) {
                return wall.gety();
            }
        }
        
        return closestPoint;
    }

    public double[] getData() {
        double temp3 = getPointForClosestWall() - Player.py;
        //System.out.print("  First: " + temp3 + "  WallDistance: " + getNextWallDistance() + "  Player Vel: " + Player.pyvel);
        return new double[] {
                Player.py,
                getPointForClosestWall() - Player.py,
                getNextWallDistance(),
                Player.pyvel
        };

    }
    
    
    public boolean getTimer(){
        return !restart;
    }

    public void restart(){
        generations++;
        //paint(getGraphics());
        restarted = true;
        restart = false;
        jumpParts.removeAll(jumpParts);
        walls.removeAll(walls);
        lowerwalls.removeAll(lowerwalls);
        Player.reset();
        timer.start();
        startTimer();
    }

    public int getReward(){
        int score = Player.score;

        if(restarted) {
            restarted = false;
            return -50; // Strong penalty for dying
        }

        if(score > tempScore){
            tempScore = score;
            return 200; // Increase reward for passing through walls
        }

        if(Player.py < 0){
            return -10; // Stronger penalty for flying too high
        }

        if(Player.py > 1000){
            return -10; // Stronger penalty for flying too low
        }

        return calculateAlignmentReward(Player.py, getPointForClosestWall(), 500) + 1; // Small reward for staying alive based off alignment
    }

    private int calculateAlignmentReward(double birdY, double holeCenterY, double maxDistance) {
        // Calculate the absolute distance between the bird and the center of the hole
        double distance = Math.abs(birdY - holeCenterY);

        // Normalize the distance: if the distance is greater than maxDistance, cap it
        if (distance > maxDistance) {
            distance = maxDistance;
        }

        // Map the distance to a reward between 0 and 5 (closer distance gets higher reward)
        // This line scales the reward: 5 for perfect alignment, 0 for maximum misalignment
        double reward = 5 * (1 - (distance / maxDistance));

        return (int) reward;
    }


    public void startTimer(){
        timer.restart();
    }
    public int distanceBetweenPoint(){
        return getPointForClosestWall() - Player.py;
    }
    public int getMaxScore(){
        return maxScore;
    }


    public void update() {
        tickCount++;

        if(Player.score > maxScore){
            maxScore = Player.score;
        }


        // Create new walls and add them to the list periodically
        if(!restart) {
            boolean temp = checkCollisions();
            period--;
        if (period <= 0) {
            point = (int) (Math.floor(Math.random() * (getHeight() - (space*2) - 30)) + space - 30);
            int wallHeight = height; // Adjust the wall height as needed
            walls.add(new Wall(getWidth(), point, width, wallHeight));//TODO
            lowerwalls.add(new LowerWall(getWidth(), point, width, wallHeight));
            period = 100; // Adjust the period for wall creation
        }
    }

        //All levels, 1-6 currently
        if(Player.score < 10) {
            level = 1;
            space = 250;
            for (Wall wall : walls) {
                wall.speed = 5;
            }
        
            for (LowerWall lowerWall : lowerwalls) {
                lowerWall.speed = 5;
            }
            
        }
        else if((Player.score >= 10) && (Player.score < 20)) {
            level = 2;
            space = 225;
            for (Wall wall : walls) {
                wall.speed = 6;
            }
        
            for (LowerWall lowerWall : lowerwalls) {
                lowerWall.speed = 6;
            }
        }
        
        else if((Player.score >= 20) && (Player.score < 30)) {
            level = 3;
            space = 200;
            for (Wall wall : walls) {
                wall.speed = 8;
            }
        
            for (LowerWall lowerWall : lowerwalls) {
                lowerWall.speed = 8;
            }
        }
        else if((Player.score >= 30) && (Player.score < 40)) {
            level = 4;
            space = 150;
            for (Wall wall : walls) {
                wall.speed = 10;
            }
        
            for (LowerWall lowerWall : lowerwalls) {
                lowerWall.speed = 10;
            }
        }

        else if((Player.score >= 40) && (Player.score < 50)) {
            level = 5;
            space = 125;
            for (Wall wall : walls) {
                wall.speed = 12;
            }
        
            for (LowerWall lowerWall : lowerwalls) {
                lowerWall.speed = 12;
            }
        }

        else if(Player.score >= 50) {
            level = 6;
            space = 100;
            for (Wall wall : walls) {
                wall.speed = 14;
            }
        
            for (LowerWall lowerWall : lowerwalls) {
                lowerWall.speed = 14;
            }
        }
        walls.removeIf(wall -> wall.getX() < -width);
        lowerwalls.removeIf(lowerWall -> lowerWall.getX() < -width);
        // Move the walls and handle scoring
        for (Wall wall : walls) {
            wall.move();
            if (wall.getX() > Player.px - 30 && wall.getX() < Player.px + 30 && !wall.isScored) {
                wall.isScored = true;
                Player.score++;
            }
            if (wall.getX() < -width) {
                walls.remove(wall); // Safely remove the wall
            }
        }

        for (LowerWall lowerWall : lowerwalls) {
            lowerWall.move();
            if (lowerWall.getX() < -width) {
                lowerwalls.remove(lowerWall); // Safely remove the lower wall
            }
        }



        for (Particles particles : jumpParts) {
            particles.lifeTime--;
            particles.move();
            if (particles.getLifeTime() < 0) {
                jumpParts.remove(particles); // Safely remove the particle
            }
        }
        // player gravity
        if(Player.isJump) {
        if(Player.spacer) {
        Player.pyvel += Player.pygrav/2;
        Player.py -= Player.pyvel;
        }
        else{
            Player.spacer = true;
        }
    }

//remove after off screen or after time is up, or game restarted

        jumpParts.removeIf(Particles-> Particles.getLifeTime() < 0);
        walls.removeIf(wall -> wall.getX() < -width);
        lowerwalls.removeIf(lowerWall -> lowerWall.getX() < -width);

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Draw the walls
        for (Wall wall : walls) {
            wall.draw(g);
        }
    
        for (LowerWall lowerWall : lowerwalls) {
            lowerWall.draw(g);
        }

        for(Particles particles : jumpParts) {
           // Graphics2D g2d = (Graphics2D) g;
           // AffineTransform oldTransform = g2d.getTransform(); // Save the current transform state
           // g2d.rotate(particles.angle, particles.x + particles.xw / 2, particles.y + particles.xh / 2); // Rotate around the center of the particle
           // g2d.setColor(Color.white);
           // g2d.fillRect(particles.x, particles.y, particles.xw, particles.xh);
          //  g2d.setTransform(oldTransform);
            //System.out.println("particles drawn");


            //particles.draw(g);
        }

         g.fillRect(Player.px, Player.py, 50, 50);
         g.fillRect(0, 800, getWidth(), 50);
         drawScore(g);
    }

}

class Floor {
    public static Rectangle getFloorBoundingBox(Drawing drawing) {
        return new Rectangle(0, 800, drawing.getWidth(), 500); // Adjust the size as needed
    }
}

class Player {
    public static boolean isJump = true;
    public static int score = 0; 
    public static boolean spacer = false; 
    public static double pygrav = -0.75;               
    public static double pyvel = 0;
    public static int px = 200;
    public static int py = 500;

    public static void reset() {
        //isJump = false;
        pyvel = 0;
        py = 500;
        score = 0;

    }

    public static Rectangle getPlayerBoundingBox() {
        return new Rectangle(px, py, 50, 50); // Adjust the size as needed
    }

    public static void jump(List <Particles> jumpParts) {
       // System.out.println(py);
        Player.pyvel = 12;
        Player.isJump = true;
        for(int i = 0; i < 50; i++) {
         jumpParts.add(new Particles());
        }

    }





}

class Wall {
    public boolean isScored = false; 
    private int x;
    private int yval;
    private int y;
    private int width;
    private int height;
    public int speed = 5;
    public Rectangle getWallBoundingBox() {
        return new Rectangle(x, y, width, height);
    }
    public Wall(int x, int y, int width, int height) {
        this.x = x;
        this.height = height;
        this.y = (y - Drawing.space) - height;
        this.width = width;
        this.yval = y;
    }

    public int gety(){
        return yval;
    }
    public int getX() {
        return x;
    }

    public void move() {
        x -= speed;
    }

    public void draw(Graphics g) {
        g.setColor(Color.green); // Adjust the wall color as needed
        g.fillRect(x, y, width, height);
    }

}

class LowerWall {
    private int x;
    private int y;
    private int width;
    private int height;
    public int speed = 5;

      public Rectangle getWallBoundingBox() {
         return new Rectangle(x, y, width, height);
     }
     
       public LowerWall(int x, int y, int width, int height) {
            this.x = x;
            this.y = y + Drawing.space;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public void move() {
            x -= speed;
        }    
        
        public void draw(Graphics h) {
            h.setColor(Color.green); // Adjust the wall color as needed
            h.fillRect(x, y, width, height);
        }


}

class Particles {
    public int lifeTime;
    public int xw = 20;
    public int xh = 20;
    public int x;
    public int y;
    public double dir;
    public double xvel;
    public double yvel; 
    public double angle = Math.toDegrees(5);
    public double angler; 

    public int getLifeTime() {
        return lifeTime; 
    }
   public Particles() {
        this.lifeTime = (int) (Math.random() * 21);
        this.xw = (int) (Math.random()*xw);
        this.xh = (int) (Math.random()*xh);
        this.x = Player.px + 20;
        this.y = Player.py - 20;
        this.dir = 250.0;
        this.xvel = (int) ((Math.random() * -9));
        this.yvel = (int) ((Math.random() * 6));
        if(Math.random() > 0.5) {
        this.angler = angle;
        }
        else{
            this.angler = -angle; 
        }    
    }
    public void move() {
        x += xvel;  
        y += yvel; 
        angle += angler; 
    }
    public void draw(Graphics h) {
        h.setColor(Color.white); 
        h.fillRect(x, y, xw, xh);
    }
}
