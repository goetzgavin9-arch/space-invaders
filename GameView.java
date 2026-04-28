import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.util.List;

public class GameView extends JPanel {
    // This class will handle the graphical representation of the game.
    // It will draw the invaders, player, bullets, score, etc.
    // Extends JPanel to be added to a JFrame.

    private GameModel model;

    public GameView(GameModel model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Set background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (model.isGameOver()) {
            // Draw game over message
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String message = "GAME OVER";
            int x = (getWidth() - g.getFontMetrics().stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g.drawString(message, x, y);
            return;
        }

        // Draw player
        g.setColor(Color.GREEN);
        g.fillRect(model.getPlayerX(), getHeight() - 50, 50, 20);

        // Draw aliens
        Graphics2D g2 = (Graphics2D) g;
        boolean[][] aliens = model.getAliens();
        for (int i = 0; i < aliens.length; i++) {
            for (int j = 0; j < aliens[i].length; j++) {
                if (aliens[i][j]) {
                    int alienX = j * 60 + 50 + model.getAlienXOffset();
                    int alienY = i * 40 + model.getAlienY();
                    if (model.isShooterAlien(i, j)) {
                        g2.setColor(Color.CYAN);
                        int[] xPoints = {alienX, alienX + 50, alienX + 25};
                        int[] yPoints = {alienY + 30, alienY + 30, alienY};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.fillRect(alienX + 10, alienY + 15, 30, 15);
                        g2.setColor(Color.BLACK);
                        g2.fillOval(alienX + 15, alienY + 18, 8, 8);
                        g2.fillOval(alienX + 27, alienY + 18, 8, 8);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.fillOval(alienX, alienY, 50, 30);
                        g2.setColor(Color.BLACK);
                        g2.fillOval(alienX + 12, alienY + 8, 8, 8);
                        g2.fillOval(alienX + 30, alienY + 8, 8, 8);
                    }
                }
            }
        }

        // Draw UFO
        if (model.isUfoActive()) {
            g.setColor(Color.MAGENTA);
            g.fillOval(model.getUfoX(), model.getUfoY(), model.getUfoWidth(), model.getUfoHeight());
        }

        // Draw player bullet
        g.setColor(Color.YELLOW);
        GameModel.Bullet playerBullet = model.getPlayerBullet();
        if (playerBullet != null) {
            g.fillRect(playerBullet.x - 2, playerBullet.y - 10, 4, 10);
        }

        // Draw alien bullets
        g.setColor(Color.RED);
        List<GameModel.Bullet> alienBullets = model.getAlienBullets();
        for (GameModel.Bullet b : alienBullets) {
            g.fillRect(b.x - 2, b.y, 4, 10);
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + model.getScore(), 10, 30);

        // Draw high score
        g.drawString("High Score: " + model.getHighScore(), 10, 60);

        // Draw lives
        g.drawString("Lives: " + model.getLives(), getWidth() - 100, 30);
    }
}
