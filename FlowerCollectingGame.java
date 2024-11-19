import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class FlowerCollectingGame extends JPanel implements ActionListener, KeyListener {

    private Timer gameLoop;
    private Timer obstacleSpawner;
    private Timer flowerSpawner;

    private int playerY = 250, playerVelY = 0;
    private boolean isJumping = false;
    private ArrayList<Rectangle> obstacles;
    private ArrayList<Rectangle> flowers;
    private int score = 0;
    private boolean gameOver = false;
    private BufferedImage flowerImage; // Image for the flower

    public FlowerCollectingGame() {
        setFocusable(true);
        addKeyListener(this);

        obstacles = new ArrayList<>();
        flowers = new ArrayList<>();

        try {
            flowerImage = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\flower.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        gameLoop = new Timer(20, this); // 50 FPS game loop
        gameLoop.start();

        obstacleSpawner = new Timer(1500, e -> generateObstacle());
        obstacleSpawner.start();

        flowerSpawner = new Timer(2000, e -> generateFlower());
        flowerSpawner.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background and ground
        Color skyColor = interpolateColor(Color.CYAN, new Color(255, 165, 0), score, 80, 130);
        Color groundColor = interpolateColor(Color.GREEN, new Color(139, 69, 19), score, 80, 130);
        Color leavesColor = interpolateColor(Color.GREEN, new Color(165, 42, 42), score, 80, 130);

        // Background and ground
        g.setColor(skyColor);
        g.fillRect(0, 0, 800, 400);
        g.setColor(groundColor);
        g.fillRect(0, 300, 800, 100);

        // Player
        drawHedgehog(g, 50, playerY);

        // Obstacles
        for (Rectangle obstacle : obstacles) {
            if (obstacle.height == 30) {
                drawDetailedRock(g, obstacle.x, obstacle.y);
            } else {
                g.setColor(new Color(102, 51, 0)); // Brown trunk
                g.fillRect(obstacle.x, obstacle.y + 40, 10, 30);
                g.setColor(leavesColor); // Green leaves
                g.fillOval(obstacle.x - 10, obstacle.y, 30, 50);
            }
        }

        // Flowers
        for (Rectangle flower : flowers) {
            drawFlower(g, flower.x, flower.y);
        }

        // Score
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 700, 20);

        // Game Over
        if (gameOver) {
            g.setFont(new Font("Courier", Font.BOLD, 36));
            g.drawString("Game Over", 300, 200);
        }
    }

    private Color interpolateColor(Color start, Color end, int score, int minScore, int maxScore) {
        if (score < minScore) {
            return start;
        } else if (score > maxScore) {
            return end;
        } else {
            float ratio = (float) (score - minScore) / (maxScore - minScore);
            int red = (int) (start.getRed() * (1 - ratio) + end.getRed() * ratio);
            int green = (int) (start.getGreen() * (1 - ratio) + end.getGreen() * ratio);
            int blue = (int) (start.getBlue() * (1 - ratio) + end.getBlue() * ratio);
            return new Color(red, green, blue);
        }
    }

    private void drawFlower(Graphics g, int x, int y) {
        if (flowerImage != null) {
            g.drawImage(flowerImage, x, y, 30, 30, null);
        }
    }

    private void drawHedgehog(Graphics g, int x, int y) {
        g.setColor(new Color(139, 69, 19));
        g.fillOval(x, y + 20, 50, 30);
        g.setColor(new Color(105, 55, 15));
        for (int i = 0; i < 5; i++) {
            int spikeX = x + 10 + (i * 8);
            g.fillPolygon(new int[]{spikeX, spikeX + 5, spikeX + 10}, new int[]{y + 25, y + 15, y + 25}, 3);
        }
        g.setColor(new Color(160, 82, 45));
        g.fillOval(x + 35, y + 5, 20, 20);
        g.setColor(Color.BLACK);
        g.fillOval(x + 45, y + 10, 5, 5);
        g.fillOval(x + 52, y + 15, 5, 5);
    }

    private void drawDetailedRock(Graphics g, int x, int y) {
        g.setColor(Color.GRAY);
        g.fillOval(x, y, 30, 20);
        g.setColor(new Color(100, 100, 100));
        g.fillOval(x + 5, y + 5, 10, 8);
        g.fillOval(x + 15, y + 8, 8, 6);
        g.setColor(new Color(180, 180, 180));
        g.fillOval(x + 10, y + 3, 8, 5);
        g.fillOval(x + 18, y + 6, 6, 4);
    }

    private void generateObstacle() {
        if (gameOver) return;

        Random rand = new Random();
        int x = 800;
        int y = 250;

        if (rand.nextBoolean()) { // Alternate between tree and rock
            obstacles.add(new Rectangle(x, y + 30, 30, 30)); // Rock
        } else {
            obstacles.add(new Rectangle(x, y - 20, 30, 40)); // Tree
        }

        if (obstacles.size() > 10) {
            obstacles.remove(0);
        }
    }

    private void generateFlower() {
        if (gameOver) return;
    
        Random rand = new Random();
        int minDistance = 50; // Minimum distance in pixels from any obstacle
    
        int x, y;
        boolean tooClose;
        do {
            tooClose = false;
            x = 800 + rand.nextInt(200);
            y = 250; // Ground level
    
            for (Rectangle obstacle : obstacles) {
                if (obstacle != null) {
                    double distance = Math.sqrt(Math.pow(x - obstacle.x, 2) + Math.pow(y - obstacle.y, 2));
                    if (distance < minDistance) {
                        tooClose = true;
                        break;
                    }
                }
            }
        } while (tooClose);
    
        flowers.add(new Rectangle(x, y, 20, 20));
    
        if (flowers.size() > 5) {
            flowers.remove(0);
        }
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Gravity and jumping
        if (isJumping) {
            playerVelY = -15;
            isJumping = false;
        } else {
            playerVelY += 1; // Simulate gravity
        }

        playerY += playerVelY;
        if (playerY > 250) {
            playerY = 250;
            playerVelY = 0;
        }

        // Move obstacles
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            obstacle.x -= 10;

            if (obstacle.x + obstacle.width < 0) {
                obstacles.remove(i);
                i--;
            } else if (obstacle.intersects(new Rectangle(50, playerY, 50, 50))) {
                gameOver = true;
                gameLoop.stop();
                obstacleSpawner.stop();
                flowerSpawner.stop();
            }
        }

        // Move flowers
        for (int i = 0; i < flowers.size(); i++) {
            Rectangle flower = flowers.get(i);
            flower.x -= 10;

            if (flower.x + flower.width < 0) {
                flowers.remove(i);
                i--;
            } else if (flower.intersects(new Rectangle(50, playerY, 50, 50))) {
                score += 10; // Collect flower
                flowers.remove(i);
                i--;
            }
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameOver && playerY == 250) {
                isJumping = true;
            }

            if (gameOver) {
                playerY = 250;
                playerVelY = 0;
                obstacles.clear();
                flowers.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                obstacleSpawner.start();
                flowerSpawner.start();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flower Collecting Game");
        FlowerCollectingGame game = new FlowerCollectingGame();
        frame.add(game);
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
