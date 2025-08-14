import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public GamePanel() {
        setBackground(Color.BLACK); 
        setFocusable(true);         
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
    }
}
