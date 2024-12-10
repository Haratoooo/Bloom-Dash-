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
    private BufferedImage flowerImage;
    private BufferedImage fallTreeImage;
    private BufferedImage fallRockImage;
    private BufferedImage fallFlowerImage;
    private BufferedImage spritesheet;
    private BufferedImage[] frames; // Sprite animation frames
    private BufferedImage currentFrame;
    private int frameIndex = 0;
    private int runAnimationCounter = 0, runAnimationSpeed = 10;

    private String season = "summer"; // Default season

    public FlowerCollectingGame() {
        setFocusable(true);
        addKeyListener(this);
        obstacles = new ArrayList<>();
        flowers = new ArrayList<>();
    
        try {
            spritesheet = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\hedgehiog.png")); // Path to sprite sheet
            extractFrames();
            flowerImage = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\flower.png"));
            fallFlowerImage = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\fall_flower.png"));
            fallTreeImage = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\fall_tree.png"));
            fallRockImage = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\fall_rock.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        gameLoop = new Timer(20, this); // 50 FPS game loop
        gameLoop.start();
    
        obstacleSpawner = new Timer(1500, e -> generateObstacle());
        obstacleSpawner.start();
    
        flowerSpawner = new Timer(1500, e -> generateFlower());
        flowerSpawner.start();
    
        // Initialize the first frame
        if (frames != null && frames.length > 0) {
            currentFrame = frames[1]; // Default to running frame
        }
    }
    
    private void extractFrames() {
        if (spritesheet == null) {
            System.out.println("Spritesheet is null, cannot extract frames.");
            return;
        }
    
        frames = new BufferedImage[4];
        frames[0] = resizeImage(spritesheet.getSubimage(24, 121, 100, 65), 70, 53);  // Frame 1 (Jumping)
        frames[1] = resizeImage(spritesheet.getSubimage(149, 121, 100, 65), 70, 53); // Frame 2 (Running 1)
        frames[2] = resizeImage(spritesheet.getSubimage(275, 121, 100, 65), 70, 53); // Frame 3 (Running 2)
        frames[3] = resizeImage(spritesheet.getSubimage(24, 217, 100, 65), 70, 53);  // Frame 4 (Dead)
    }

    private BufferedImage resizeImage(BufferedImage original, int newWidth, int newHeight) {
        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return resized;
    }

    private void updateAnimation() {
        if (gameOver) {
            currentFrame = frames[3]; // Dead frame
        } else if (playerY < 250) {
            currentFrame = frames[0]; // Jumping frame
        } else {
            runAnimationCounter++;
            if (runAnimationCounter >= runAnimationSpeed) {
                frameIndex = (frameIndex == 1) ? 2 : 1; // Toggle between running frames
                currentFrame = frames[frameIndex];
                runAnimationCounter = 0;
            }
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background and ground
        Color skyColor = interpolateColor(Color.CYAN, new Color(204, 234, 215), score, 50, 100);
        Color groundColor = interpolateColor(Color.GREEN, new Color(135, 114, 0), score, 50, 100);
        Color leavesColor = interpolateColor(Color.GREEN, new Color(165, 42, 42), score, 50, 100);

        g.setColor(skyColor);
        g.fillRect(0, 0, 800, 400);
        g.setColor(groundColor);
        g.fillRect(0, 300, 800, 100);

        // Player
        if (currentFrame != null) {
            g.drawImage(currentFrame, 50, playerY, null);
        }

        // Obstacles
        for (Rectangle obstacle : obstacles) {
            if (season.equals("fall")) {
                if (obstacle.height == 30) {
                    g.drawImage(fallRockImage, obstacle.x, obstacle.y - 20, 50, 50, null); // Fall rock
                } else {
                    g.drawImage(fallTreeImage, obstacle.x, obstacle.y - 10, 70, 90, null); // Fall tree
                }
            } else {
                if (obstacle.height == 30) {
                    drawDetailedRock(g, obstacle.x, obstacle.y);
                } else {
                    g.setColor(new Color(102, 51, 0)); // Brown trunk
                    g.fillRect(obstacle.x, obstacle.y + 40, 10, 30);
                    g.setColor(leavesColor); // Green leaves
                    g.fillOval(obstacle.x - 10, obstacle.y, 30, 50);
                }
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

        if (rand.nextBoolean()) {
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
        int x = 900 + rand.nextInt(300);
        int y = 250;

        flowers.add(new Rectangle(x, y, 20, 20));

        if (flowers.size() > 5) {
            flowers.remove(0);
        }
    }

   public void setSeason(String newSeason) {
    season = newSeason;
    // Update flower image when the season changes to fall
    if (season.equals("fall")) {
        try {
            flowerImage = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\fall_flower.png")); // Fall flower image
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception by printing the stack trace
        }
    } else {
        try {
            flowerImage = ImageIO.read(new File("D:\\SY 2024-2025\\Bloom Dash!\\pics\\flower.png"));
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception by printing the stack trace
        }
    }
    repaint();
}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        if (isJumping) {
            playerVelY = -15;
            isJumping = false;
        } else {
            playerVelY += 1;
        }

        playerY += playerVelY;
        if (playerY > 250) {
            playerY = 250;
            playerVelY = 0;
        }

        updateAnimation();

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

        for (int i = 0; i < flowers.size(); i++) {
            Rectangle flower = flowers.get(i);
            flower.x -= 10;

            if (flower.x + flower.width < 0) {
                flowers.remove(i);
                i--;
            } else if (flower.intersects(new Rectangle(50, playerY, 50, 50))) {
                score += 10;
                flowers.remove(i);
                i--;
            }
        }

        if (score >= 100 && !season.equals("fall")) {
            setSeason("fall");
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
