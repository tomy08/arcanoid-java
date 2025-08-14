package arcanoid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private java.util.List<Brick> bricks = new java.util.ArrayList<>();
	private int score = 0;

	
	private int paddleX, paddleY, paddleWidth, paddleHeight;
	
	private int ballX, ballY, ballSize;
	private double ballDX = 4; // velocidad horizontal
	private double ballDY = -4; // velocidad vertical

	
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean started = false;
	
	private boolean initialized = false;
	// Se inicializan las variables de paddle y de la pelota en addNotify(porque no estan disponibles en el constructor)
	@Override
	public void addNotify() {
	    super.addNotify();
	    int panelWidth = getWidth();
	    int panelHeight = getHeight();

	    // Paleta centrada horizontalmente, cerca del borde inferior
	    paddleWidth = 100;
	    paddleHeight = 10;
	    paddleX = (panelWidth - paddleWidth) / 2;
	    paddleY = panelHeight - paddleHeight - 50;

	    // Pelota centrada sobre la paleta
	    ballSize = 10;
	    ballX = paddleX + (paddleWidth - ballSize) / 2;
	    ballY = paddleY - ballSize - 2;
	}

	
	private void generateLevel() {
	    bricks.clear();
	    int brickRows = 5;
	    int brickCols = getWidth() / 60; // ladrillos de 50px con 10px de gap
	    int brickWidth = (getWidth() - (brickCols + 1) * 10) / brickCols;
	    int brickHeight = 20;

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
            	
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                
                boolean validation = e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D;
                if (!started && validation) {
                    started = true;
                    // faltan cositass
                }
                
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                    leftPressed = true;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT  || e.getKeyCode() == KeyEvent.VK_D) {
                    rightPressed = true;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                    leftPressed = false;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                    rightPressed = false;
                }
            }
        });

        // Linea para que haya aprox. 60fps
        new javax.swing.Timer(16, e -> {
            if (leftPressed) paddleX -= 6;
            if (rightPressed) paddleX += 6;
           
            // mantener paddle dentro de la pantalla
            paddleX = Math.max(0, Math.min(getWidth() - paddleWidth, paddleX));

            // mover pelota solamente si el juego comenzo
            if (started) {
                ballX += ballDX;
                ballY += ballDY;

                // rebote en paredes
                if (ballX <= 0 || ballX + ballSize >= getWidth()) ballDX = -ballDX;
                if (ballY <= 0) ballDY = -ballDY;

                // rebote con la paleta
                if (ballY + ballSize >= paddleY && ballX + ballSize >= paddleX && ballX <= paddleX + paddleWidth) {
                    ballDY = -ballDY;

                    // ajusta la direccion segun donde pega en la paleta
                    double hitPos = (ballX + ballSize/2.0) - (paddleX + paddleWidth/2.0);
                    ballDX = hitPos * 0.1; // mientras mas a los lados, mas angulo
                }
                
                for (Brick brick : bricks) {
                    if (!brick.destroyed &&
                        ballX + ballSize > brick.x &&
                        ballX < brick.x + brick.width &&
                        ballY + ballSize > brick.y &&
                        ballY < brick.y + brick.height) {

                        brick.destroyed = true;
                        ballDY = -ballDY; // rebote
                        score += 10;
                        break; // solo un ladrillo por movimiento
                    }
                }
                boolean allDestroyed = bricks.stream().allMatch(b -> b.destroyed);
                if (allDestroyed) {
                    generateLevel();
                    // opcional: aumentar velocidad de la pelota
                    if (ballDY > 0) ballDY += 0.5;
                    else ballDY -= 0.5;
                }

                // si la pelota cae del borde inferior, reset
                if (ballY > getHeight()) {
                    started = false;
                    ballX = paddleX + (paddleWidth - ballSize)/2;
                    ballY = paddleY - ballSize - 2;
                    ballDX = 4;
                    ballDY = -4;
                }
            }
            
        


            repaint();
        }).start();
    }
	
	

    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);

    	// Panel, paddle y pelota
        if (!initialized) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            paddleWidth = 100;
            paddleHeight = 10;
            paddleX = (panelWidth - paddleWidth) / 2;
            paddleY = panelHeight - paddleHeight - 50;

            ballSize = 10;
            ballX = paddleX + (paddleWidth - ballSize) / 2;
            ballY = paddleY - ballSize - 2;

            initialized = true;
        }

        // Ladrillos
        g.setColor(Color.WHITE);
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);
        g.fillOval(ballX, ballY, ballSize, ballSize);
        
        g.setColor(Color.RED);
        for (Brick brick : bricks) {
            if (!brick.destroyed) {
                g.fillRect(brick.x, brick.y, brick.width, brick.height);
            }
        }

        // mostrar puntuacion
        g.setColor(Color.WHITE);
        g.drawString("Puntuación: " + score, 10, 20);

    }
    
  
    

}
