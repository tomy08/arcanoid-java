package arcanoid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private int paddleX = 350;
	private int paddleY = 550;
	private int paddleWidth = 100;
	private int paddleHeight = 10;

	private int ballX = 390;
	private int ballY = 540;
	private int ballSize = 10;

	public GamePanel() {
        setBackground(Color.BLACK); 
        setFocusable(true);  
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });

    }
	
	

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        // Paleta
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);
        // Pelota
        g.fillOval(ballX, ballY, ballSize, ballSize);
    }
    
  
    

}
