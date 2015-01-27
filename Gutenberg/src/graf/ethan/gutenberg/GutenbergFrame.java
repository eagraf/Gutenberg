package graf.ethan.gutenberg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;

/*
 * A simple frame class that extends JFrame.
 */
public class GutenbergFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private GutenbergDrawer drawer;
	
	public GutenbergFrame(String title, GutenbergDrawer drawer) {
		super(title);
		this.drawer = drawer;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		/*
		g.setFont(new Font("Times New Roman", Font.PLAIN, 24));
		g.setColor(Color.BLACK);
		g.drawString("Hello World", 50, 50);*/
		drawer.drawPage(g, drawer.scanner.getPage());
	}
}
