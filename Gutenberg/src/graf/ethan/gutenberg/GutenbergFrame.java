package graf.ethan.gutenberg;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

public class GutenbergFrame extends JFrame {
	private GutenbergDrawer drawer;
	
	public GutenbergFrame(String title, GutenbergDrawer drawer) {
		super(title);
		this.drawer = drawer;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawer.drawPage(g, drawer.scanner.getPage());
	}
}
