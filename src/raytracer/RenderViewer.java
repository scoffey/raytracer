package raytracer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class RenderViewer extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public RenderViewer() {
		super("Computación Gráfica - TPE2");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public RenderViewer(BufferedImage bi) {
		this();
		Dimension d = new Dimension(bi.getWidth(), bi.getHeight()); 
		setSize(d);
		setResizable(false);
		ImageIcon image = new ImageIcon(bi);
		add(new JLabel(image));
		setVisible(true);
	}

}
