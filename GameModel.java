import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.*;

public class GameModel {
    // This class will handle the game logic, state, and data.
    // It will manage the positions of invaders, player, bullets, score, etc.
    // No Swing dependencies here.

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_SPEED = 5;
    private static final int BULLET_SPEED = 10;
    private static final int ALIEN_ROWS = 5;
    private static final int ALIEN_COLS = 11;
    private static final int ALIEN_WIDTH = 50;
    private static final int ALIEN_HEIGHT = 30;
    private static final int ALIEN_SPACING_X = 60;
    private static final int ALIEN_SPACING_Y = 40;
    private static final int ALIEN_START_X = 50;
    private static final int ALIEN_START_Y = 50;
    private static final int ALIEN_SPEED = 2;
    private static final int ALIEN_DROP = 20;
    private static final int UFO_WIDTH = 80;
    private static final int UFO_HEIGHT = 24;
    private static final int UFO_Y = 20;
    private static final int UFO_SPEED = 4;
    private static final int UFO_SPAWN_MIN = 200;
    private static final int UFO_SPAWN_MAX = 600;
    private static final int ALIEN_SPEED_STEP_COUNT = ALIEN_COLS * 2; // 2 rows worth of aliens
    private static final double ALIEN_SPEED_MULTIPLIER = 1.2;
    private static final int SHOOTER_SHOOT_INTERVAL = 60; // 3 seconds at 50ms per tick

    public static class Bullet {
        public int x, y;
        public boolean active;

        public Bullet(int x, int y) {
            this.x = x;
            this.y = y;
            this.active = true;
        }
    }

    private int playerX = WIDTH / 2;
    private boolean[][] aliens = new boolean[ALIEN_ROWS][ALIEN_COLS];
    private int alienXOffset = 0;
    private int alienDirection = 1; // 1 for right, -1 for left
    private int alienY = ALIEN_START_Y;
    private Bullet playerBullet = null;
    private List<Bullet> alienBullets = new ArrayList<>();
    private int score = 0;
    private int lives = 3;
    private int highScore = 0;
    private double alienSpeed = ALIEN_SPEED;
    private int aliensDestroyed = 0;
    private int alienSpeedStage = 0;
    private int shooterShootCountdown = SHOOTER_SHOOT_INTERVAL;
    private int[] shooterColumn = new int[ALIEN_ROWS];
    private boolean ufoActive = false;
    private int ufoX = -UFO_WIDTH;
    private int ufoSpawnCountdown;
    private Random random = new Random();

    public GameModel() {
        ufoSpawnCountdown = random.nextInt(UFO_SPAWN_MAX - UFO_SPAWN_MIN + 1) + UFO_SPAWN_MIN;
        // Initialize aliens and shooter positions
        for (int i = 0; i < ALIEN_ROWS; i++) {
            for (int j = 0; j < ALIEN_COLS; j++) {
                aliens[i][j] = true;
            }
            shooterColumn[i] = random.nextInt(ALIEN_COLS);
        }
        // Load high score
        try {
            BufferedReader br = new BufferedReader(new FileReader("highscore.txt"));
            highScore = Integer.parseInt(br.readLine());
            br.close();
        } catch (Exception e) {
            highScore = 0;
        }
    }

    public void movePlayerLeft() {
        playerX -= PLAYER_SPEED;
        if (playerX < 0) playerX = 0;
    }

    public void movePlayerRight() {
        playerX += PLAYER_SPEED;
        if (playerX > WIDTH - PLAYER_WIDTH) playerX = WIDTH - PLAYER_WIDTH;
    }

    public void firePlayerBullet() {
        if (playerBullet == null) {
            playerBullet = new Bullet(playerX + PLAYER_WIDTH / 2, HEIGHT - 50);
        }
    }

    public void update() {
        // Advance player bullet
        if (playerBullet != null) {
            playerBullet.y -= BULLET_SPEED;
            if (playerBullet.y < 0) {
                playerBullet = null;
            }
        }

        // Advance UFO
        if (ufoActive) {
            ufoX += UFO_SPEED;
            if (ufoX > WIDTH) {
                ufoActive = false;
                ufoX = -UFO_WIDTH;
                ufoSpawnCountdown = random.nextInt(UFO_SPAWN_MAX - UFO_SPAWN_MIN + 1) + UFO_SPAWN_MIN;
            }
        } else {
            ufoSpawnCountdown--;
            if (ufoSpawnCountdown <= 0) {
                ufoActive = true;
                ufoX = -UFO_WIDTH;
            }
        }

        // Advance alien bullets
        for (Bullet b : alienBullets) {
            b.y += BULLET_SPEED;
            if (b.y > HEIGHT) {
                b.active = false;
            }
        }
        alienBullets.removeIf(b -> !b.active);

        // Move aliens
        boolean hitEdge = false;
        for (int i = 0; i < ALIEN_ROWS; i++) {
            for (int j = 0; j < ALIEN_COLS; j++) {
                if (aliens[i][j]) {
                    int alienX = j * ALIEN_SPACING_X + ALIEN_START_X + alienXOffset;
                    if (alienDirection == 1 && alienX + ALIEN_WIDTH >= WIDTH) {
                        hitEdge = true;
                    }
                    if (alienDirection == -1 && alienX <= 0) {
                        hitEdge = true;
                    }
                }
            }
        }
        if (hitEdge) {
            alienDirection *= -1;
            alienY += ALIEN_DROP;
        }
        alienXOffset += alienDirection * alienSpeed;

        // Shooter alien bullets every interval
        shooterShootCountdown--;
        if (shooterShootCountdown <= 0) {
            shooterShootCountdown = SHOOTER_SHOOT_INTERVAL;
            for (int i = 0; i < ALIEN_ROWS; i++) {
                int shooterCol = getShooterColumn(i);
                if (shooterCol >= 0) {
                    int shooterX = shooterCol * ALIEN_SPACING_X + ALIEN_START_X + alienXOffset;
                    int shooterY = i * ALIEN_SPACING_Y + alienY;
                    alienBullets.add(new Bullet(shooterX + ALIEN_WIDTH / 2, shooterY + ALIEN_HEIGHT));
                }
            }
        }

        // Check collisions: player bullet vs UFO
        if (ufoActive && playerBullet != null) {
            if (playerBullet.x >= ufoX && playerBullet.x <= ufoX + UFO_WIDTH &&
                playerBullet.y >= UFO_Y && playerBullet.y <= UFO_Y + UFO_HEIGHT) {
                ufoActive = false;
                playerBullet = null;
                score += 30;
                if (score > highScore) {
                    highScore = score;
                    try {
                        PrintWriter pw = new PrintWriter(new FileWriter("highscore.txt"));
                        pw.println(highScore);
                        pw.close();
                    } catch (Exception e) {}
                }
                ufoX = -UFO_WIDTH;
                ufoSpawnCountdown = random.nextInt(UFO_SPAWN_MAX - UFO_SPAWN_MIN + 1) + UFO_SPAWN_MIN;
            }
        }

        // Check collisions: player bullet vs aliens
        outer: if (playerBullet != null) {
            for (int i = 0; i < ALIEN_ROWS; i++) {
                for (int j = 0; j < ALIEN_COLS; j++) {
                    if (aliens[i][j]) {
                        int alienX = j * ALIEN_SPACING_X + ALIEN_START_X + alienXOffset;
                        int alienYPos = i * ALIEN_SPACING_Y + alienY;
                        if (playerBullet.x >= alienX && playerBullet.x <= alienX + ALIEN_WIDTH &&
                            playerBullet.y >= alienYPos && playerBullet.y <= alienYPos + ALIEN_HEIGHT) {
                            aliens[i][j] = false;
                            if (j == shooterColumn[i]) {
                                shooterColumn[i] = chooseRandomShooterInRow(i);
                            }
                            playerBullet = null;
                            score += 10;
                            aliensDestroyed++;
                            int newSpeedStage = aliensDestroyed / ALIEN_SPEED_STEP_COUNT;
                            if (newSpeedStage > alienSpeedStage) {
                                alienSpeedStage = newSpeedStage;
                                alienSpeed *= ALIEN_SPEED_MULTIPLIER;
                            }
                            if (score > highScore) {
                                highScore = score;
                                try {
                                    PrintWriter pw = new PrintWriter(new FileWriter("highscore.txt"));
                                    pw.println(highScore);
                                    pw.close();
                                } catch (Exception e) {}
                            }
                            break outer;
                        }
                    }
                }
            }
        }

        // Check collisions: alien bullets vs player
        for (Bullet b : alienBullets) {
            if (b.x >= playerX && b.x <= playerX + PLAYER_WIDTH &&
                b.y >= HEIGHT - 50 && b.y <= HEIGHT) {
                lives--;
                b.active = false;
                // Optionally reset player position or add invincibility frames
            }
        }
        alienBullets.removeIf(b -> !b.active);
    }

    // Getters for the view
    public int getPlayerX() {
        return playerX;
    }

    public boolean[][] getAliens() {
        return aliens;
    }

    public int getAlienXOffset() {
        return alienXOffset;
    }

    public int getAlienY() {
        return alienY;
    }

    public boolean isUfoActive() {
        return ufoActive;
    }

    public int getUfoX() {
        return ufoX;
    }

    public int getUfoY() {
        return UFO_Y;
    }

    public int getUfoWidth() {
        return UFO_WIDTH;
    }

    public int getUfoHeight() {
        return UFO_HEIGHT;
    }

    public Bullet getPlayerBullet() {
        return playerBullet;
    }

    public List<Bullet> getAlienBullets() {
        return alienBullets;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getHighScore() {
        return highScore;
    }

    public boolean isGameOver() {
        return lives <= 0 || alienY + ALIEN_ROWS * ALIEN_SPACING_Y >= HEIGHT - 100; // Aliens reached bottom
    }

    public boolean isShooterAlien(int row, int col) {
        return col == getShooterColumn(row) && aliens[row][col];
    }

    private int getShooterColumn(int row) {
        if (row < 0 || row >= ALIEN_ROWS) {
            return -1;
        }
        if (shooterColumn[row] >= 0 && shooterColumn[row] < ALIEN_COLS && aliens[row][shooterColumn[row]]) {
            return shooterColumn[row];
        }
        shooterColumn[row] = chooseRandomShooterInRow(row);
        return shooterColumn[row];
    }

    private int chooseRandomShooterInRow(int row) {
        List<Integer> alive = new ArrayList<>();
        for (int j = 0; j < ALIEN_COLS; j++) {
            if (aliens[row][j]) {
                alive.add(j);
            }
        }
        if (alive.isEmpty()) {
            return -1;
        }
        return alive.get(random.nextInt(alive.size()));
    }

    public void setPlayerX(int x) { playerX = x; } // For testing

    public void setLives(int l) { lives = l; } // For testing

    public void setAlienXOffset(int o) { alienXOffset = o; } // For testing

    public void addAlienBullet(int x, int y) { alienBullets.add(new Bullet(x, y)); } // For testing
}