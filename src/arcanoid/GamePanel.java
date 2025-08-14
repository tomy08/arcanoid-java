package arcanoid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private java.util.List<Brick> bricks = new java.util.ArrayList<>();
    private int score = 0;
    private int record = 0;
    private int lives = 3;

    private int paddleX, paddleY, paddleWidth, paddleHeight;
    private double paddleSpeed;
    private double ballSpeed;
    private int ballX, ballY, ballSize;
    private double ballDX, ballDY;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean started = false;
    private boolean initialized = false;

    @Override
    public void addNotify() {
        super.addNotify();
  
    }

    private void initGame() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // paddle
        paddleWidth = panelWidth / 8;
        paddleHeight = panelHeight / 40;
        paddleX = (panelWidth - paddleWidth) / 2;
        paddleY = panelHeight - paddleHeight - panelHeight / 20;

        // ball
        ballSize = panelWidth / 60;
        ballX = paddleX + (paddleWidth - ballSize) / 2;
        ballY = paddleY - ballSize - 2;

        // velocidades proporcionales
        paddleSpeed = panelWidth / 80.0; 
        ballSpeed = panelWidth / 150.0; 
        ballDX = ballSpeed;
        ballDY = -ballSpeed;

        generateLevel();
        initialized = true;
    }

    private void generateLevel() {
        bricks.clear();
        int brickRows = 5;
        int brickCols = Math.max(5, getWidth() / 100);
        int brickWidth = (getWidth() - (brickCols + 1) * 10) / brickCols;
        int brickHeight = getHeight() / 30;

        for (int row = 0; row < brickRows; row++) {
            for (int col = 0; col < brickCols; col++) {
                int x = 10 + col * (brickWidth + 10);
                int y = 50 + row * (brickHeight + 10);
                bricks.add(new Brick(x, y, brickWidth, brickHeight));
            }
        }
    }

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);

                boolean valid = e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A
                        || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D;
                if (!started && valid) started = true;

                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) leftPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) rightPressed = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) leftPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) rightPressed = false;
            }
        });

        new javax.swing.Timer(16, e -> gameLoop()).start();
    }

    private void gameLoop() {
        if (!initialized) return;

        // mover paddle
        if (leftPressed) paddleX -= paddleSpeed;
        if (rightPressed) paddleX += paddleSpeed;
        paddleX = Math.max(0, Math.min(getWidth() - paddleWidth, paddleX));

        if (started) {
            // mover pelota
            ballX += ballDX;
            ballY += ballDY;

            // rebote en paredes
            if (ballX <= 0 || ballX + ballSize >= getWidth()) ballDX = -ballDX;
            if (ballY <= 0) ballDY = -ballDY;

            // rebote con paddle
            if (ballY + ballSize >= paddleY &&
            	    ballX + ballSize >= paddleX &&
            	    ballX <= paddleX + paddleWidth) {

            	    double rel = ((ballX + ballSize / 2.0) - (paddleX + paddleWidth / 2.0)) / (paddleWidth / 2.0);
            	    double ang = rel * Math.toRadians(60);
            	    double spd = Math.sqrt(ballDX * ballDX + ballDY * ballDY);

            	    ballDX = spd * Math.sin(ang);
            	    ballDY = -spd * Math.cos(ang);
            }

            // colision con ladrillos
            for (Brick brick : bricks) {
                if (!brick.destroyed &&
                    ballX + ballSize > brick.x &&
                    ballX < brick.x + brick.width &&
                    ballY + ballSize > brick.y &&
                    ballY < brick.y + brick.height) {

                    brick.destroyed = true;
                    ballDY = -ballDY;
                    score += 10;
                    break;
                }
            }

            // si rompe todos los ladrillos
            if (bricks.stream().allMatch(b -> b.destroyed)) {
                generateLevel();
                ballDX *= 1.1;
                ballDY *= 1.1;
            }

            // si la pelota cae
            if (ballY > getHeight()) {
            	 lives--;
            	 if (lives <= 0) {
            	     if (score > record) record = score;
            	     score = 0;
            	     lives = 3;
            	     generateLevel();
            	  }
            	  started = false;
            	  ballX = paddleX + (paddleWidth - ballSize) / 2;
            	  ballY = paddleY - ballSize - 2;
            	  ballDX = ballSpeed;
            	  ballDY = -ballSpeed;
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!initialized && getWidth() > 0 && getHeight() > 0) {
            initGame();
        }

        // paddle
        g.setColor(Color.WHITE);
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);

        // pelota
        g.fillOval(ballX, ballY, ballSize, ballSize);

        // ladrillos
        g.setColor(Color.RED);
        for (Brick brick : bricks) {
            if (!brick.destroyed) {
                g.fillRect(brick.x, brick.y, brick.width, brick.height);
            }
        }

        // puntaje, vidas, record
        g.setFont(new Font("Arial", Font.BOLD, 24)); // fuente grande y negrita
        g.setColor(Color.BLACK);

        // sombra para el texto (para que resalte sobre el fondo)
        int y = 30;
        int x = 20;
        String txtPuntaje = "Puntaje: " + score;
        String txtVidas   = "Vidas: " + lives;
        String txtRecord  = "Record: " + record;

        // dibuja texto blanco encima
        g.setColor(Color.WHITE);
        x = 20;
        g.drawString(txtPuntaje, x, y);
        x += g.getFontMetrics().stringWidth(txtPuntaje) + 40;
        g.drawString(txtVidas, x, y);
        x += g.getFontMetrics().stringWidth(txtVidas) + 40;
        g.drawString(txtRecord, x, y);
        
    }
}
