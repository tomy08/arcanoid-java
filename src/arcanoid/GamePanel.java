package arcanoid;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;

class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private java.util.List<Brick> bricks = new java.util.ArrayList<>();
    private int score = 0;
    private int record = 0;
    private int lives = 3;
    private int level = 1;


    
    private File recordFile = new File("record.txt");


    private int paddleX, paddleY, paddleWidth, paddleHeight;
    private double paddleSpeed;
    private double ballSpeed;
    private double maxBallSpeed; 
    private int ballX, ballY, ballSize;
    private double ballDX, ballDY;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean started = false;
    private boolean initialized = false;
    
    private void guardarRecord() {
        try (FileWriter fw = new FileWriter(recordFile)) {
            fw.write(String.valueOf(record));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void cargarRecord() {
        if (recordFile.exists()) {
            try (Scanner sc = new Scanner(recordFile)) {
                if (sc.hasNextInt()) record = sc.nextInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playSound(String resourcePath) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(
                getClass().getResource(resourcePath)
            );
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Override
    public void addNotify() {
        super.addNotify();
        cargarRecord();

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
        maxBallSpeed = ballSpeed * 1.5; 
        ballDX = ballSpeed;
        ballDY = -ballSpeed;


        generateLevel();
        initialized = true;
    }

    private void generateLevel() {
        bricks.clear();
        int brickRows = 4 + level; // más filas según nivel
        int brickCols = Math.min(10, getWidth() / 80); // más columnas
        int spacing = 10;
        int brickWidth = (getWidth() - (brickCols + 1) * spacing) / brickCols;
        int brickHeight = getHeight() / 30;

        for (int row = 2; row < brickRows; row++) {
            for (int col = 1; col < brickCols - 1; col++) {
                int x = spacing + col * (brickWidth + spacing);
                int y = spacing + row * (brickHeight + spacing);

                // ladrillos con resistencia mayor según nivel
                int hits = 1;
                if (level >= 2 && Math.random() < 0.3) hits = 2;
                if (level >= 4 && Math.random() < 0.2) hits = 3;

                bricks.add(new Brick(x, y, brickWidth, brickHeight, hits));
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
            if (ballX <= 0 || ballX + ballSize >= getWidth()) {
            	playSound("/arcanoid/sounds/rebote.wav");
            	ballDX = -ballDX;
            }
            if (ballY <= 0) {
            	playSound("/arcanoid/sounds/rebote.wav");
            	ballDY = -ballDY;
            }

            // rebote con paddle
            if (ballY + ballSize >= paddleY &&
            	    ballX + ballSize >= paddleX &&
            	    ballX <= paddleX + paddleWidth) {

            		playSound("/arcanoid/sounds/rebote.wav");
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

                	brick.hit();
                	if (brick.isDestroyed()) {
                	    score += 10;
                	    playSound("/arcanoid/sounds/ladrillo.wav");
                	}
                	ballDY = -ballDY;
                    break;
                }
            }

            // si rompe todos los ladrillos
            if (bricks.stream().allMatch(b -> b.destroyed)) {
            	level++;
                generateLevel();
                ballDX *= 1.1;
                ballDY *= 1.1;
            }

            // si la pelota cae
            if (ballY > getHeight()) {
            	
            	 lives--;
            	 if (lives <= 0) {
            		 playSound("/arcanoid/sounds/game-over.wav");
            	     if (score > record) {
            	    	 record = score;
            	    	 guardarRecord();
            	     }
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
        
        Graphics2D g2 = (Graphics2D) g;
        
        // Fondo
        GradientPaint bgGradient = new GradientPaint(
            0, 0, Color.BLACK, 
            0, getHeight(), Color.DARK_GRAY
        );
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, getWidth(), getHeight());


        // paddle
     
        GradientPaint paddleGradient = new GradientPaint(
            paddleX, paddleY, Color.BLUE.brighter(),
            paddleX + paddleWidth, paddleY + paddleHeight, Color.BLUE.darker()
        );
        g2.setPaint(paddleGradient);
        g2.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);

        // borde oscuro para destacar
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(paddleX, paddleY, paddleWidth, paddleHeight);

        // highlight arriba
        g2.setColor(new Color(255,255,255,80));
        g2.fillRect(paddleX + 2, paddleY + 2, paddleWidth - 4, paddleHeight/3);


        // pelota
     

        // degradé circular para la pelota
        GradientPaint ballGradient = new GradientPaint(
         ballX, ballY, Color.WHITE.brighter(),
         ballX + ballSize, ballY + ballSize, Color.LIGHT_GRAY.darker()
        );
        g2.setPaint(ballGradient);
        g2.fillOval(ballX, ballY, ballSize, ballSize);

       
        g2.setColor(Color.GRAY);
        g2.drawOval(ballX, ballY, ballSize, ballSize);


        // ladrillos
        g.setColor(Color.RED);
        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                Color color;
                if (brick.hits == 1) color = Color.RED;
                else if (brick.hits == 2) color = Color.ORANGE;
                else color = Color.YELLOW;

                GradientPaint gradient = new GradientPaint(
                    brick.x, brick.y, color.brighter(),
                    brick.x + brick.width, brick.y + brick.height,
                    color.darker()
                );
                g2.setPaint(gradient);
                g2.fillRect(brick.x, brick.y, brick.width, brick.height);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(brick.x, brick.y, brick.width, brick.height);
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
        String txtlevel = "Nivel: " + level;

        // dibuja texto blanco encima
        g.setColor(Color.WHITE);
        x = 20;
        g.drawString(txtPuntaje, x, y);
        x += g.getFontMetrics().stringWidth(txtPuntaje) + 40;
        g.drawString(txtVidas, x, y);
        x += g.getFontMetrics().stringWidth(txtVidas) + 40;
        g.drawString(txtRecord, x, y);
        x += g.getFontMetrics().stringWidth(txtRecord) + 40;
        g.drawString(txtlevel, x, y);
        
    }
}
