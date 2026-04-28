import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameController {
    // This class will control the game flow, handle user input, and coordinate between model and view.

    public static void main(String[] args) {
        // Create the model
        GameModel model = new GameModel();

        // Create the view
        GameView view = new GameView(model);
        view.setFocusable(true);

        // Add keyboard controls
        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        model.movePlayerLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        model.movePlayerRight();
                        break;
                    case KeyEvent.VK_SPACE:
                        model.firePlayerBullet();
                        break;
                }
            }
        });

        // Create the JFrame
        JFrame frame = new JFrame("Space Invaders");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(view);
        frame.setSize(800, 600);
        frame.setVisible(true);

        // Start the game loop
        Timer timer = new Timer(50, e -> { // ~20 FPS
            model.update();
            view.repaint();
            if (model.isGameOver()) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
}
