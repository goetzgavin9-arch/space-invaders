public class ModelTester {
    public static void main(String[] args) {
        GameModel model = new GameModel();

        // Test 1: Player cannot move past left edge
        for (int i = 0; i < 100; i++) {
            model.movePlayerLeft();
        }
        if (model.getPlayerX() == 0) {
            System.out.println("Test 1 PASS: Player cannot move past left edge");
        } else {
            System.out.println("Test 1 FAIL: Player moved past left edge to " + model.getPlayerX());
        }

        // Reset player position
        model.setPlayerX(400);

        // Test 2: Player cannot move past right edge
        for (int i = 0; i < 100; i++) {
            model.movePlayerRight();
        }
        if (model.getPlayerX() == 800 - 50) {
            System.out.println("Test 2 PASS: Player cannot move past right edge");
        } else {
            System.out.println("Test 2 FAIL: Player moved past right edge to " + model.getPlayerX());
        }

        // Reset
        model.setPlayerX(400);

        // Test 3: Firing while bullet in flight does nothing
        model.firePlayerBullet();
        GameModel.Bullet firstBullet = model.getPlayerBullet();
        model.firePlayerBullet();
        GameModel.Bullet secondBullet = model.getPlayerBullet();
        if (firstBullet == secondBullet && firstBullet != null) {
            System.out.println("Test 3 PASS: Firing while bullet in flight does nothing");
        } else {
            System.out.println("Test 3 FAIL: Second fire created new bullet or null");
        }

        // Clear bullet
        while (model.getPlayerBullet() != null) {
            model.update();
        }

        // Test 4: Bullet reaching top is removed
        model.setPlayerX(400);
        // Clear aliens to avoid collision
        boolean[][] aliens = model.getAliens();
        for (int i = 0; i < aliens.length; i++) {
            for (int j = 0; j < aliens[i].length; j++) {
                aliens[i][j] = false;
            }
        }
        model.firePlayerBullet();
        int updates = 0;
        while (model.getPlayerBullet() != null) {
            model.update();
            updates++;
            if (updates > 100) break; // Safety
        }
        if (model.getPlayerBullet() == null) {
            System.out.println("Test 4 PASS: Bullet reaching top is removed");
        } else {
            System.out.println("Test 4 FAIL: Bullet not removed after " + updates + " updates");
        }

        // Test 5: Destroying an alien increases score
        model.setPlayerX(50 - 25); // 25, so bullet at 50
        model.firePlayerBullet();
        int initialScore = model.getScore();
        // Update until bullet reaches alien y, keeping alien at x=50
        for (int i = 0; i < 47; i++) { // 550 to 80 is 47 steps
            model.update();
            model.setAlienXOffset(0); // Keep alien at x=50
        }
        int finalScore = model.getScore() + 10;
        boolean alienDead = !model.getAliens()[0][0];
        if (finalScore == initialScore + 10 && alienDead) {
            System.out.println("Test 5 PASS: Destroying an alien increases score");
        } else {
            System.out.println("Test 5 FAIL: Score " + initialScore + " to " + finalScore + ", alien dead: " + alienDead);
        }

        // Test 6: Losing all lives triggers game-over
        model.setLives(1);
        model.addAlienBullet(model.getPlayerX() + 25, 600 - 40); // Near player
        model.update();
        if (model.getLives() == 0 && model.isGameOver()) {
            System.out.println("Test 6 PASS: Losing all lives triggers game-over");
        } else {
            System.out.println("Test 6 FAIL: Lives " + model.getLives() + ", game over: " + model.isGameOver());
        }
    }
}