package arcanoid;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class arcanoidinfinite extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					arcanoidinfinite frame = new arcanoidinfinite();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public arcanoidinfinite() {
	    setTitle("Arcanoid Infinito");
	    setUndecorated(true);
	    setExtendedState(JFrame.MAXIMIZED_BOTH); 
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    add(new GamePanel()); // nuestro panel de juego


	}

}
