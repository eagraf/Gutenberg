package graf.ethan.gutenberg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/*
 * Class responsible for the drawing of the PDF onto the screen.
 */
public class GutenbergDrawer {
	public GutenbergScanner scanner;
	
	public GutenbergDrawer(GutenbergScanner scanner) {
		this.scanner = scanner;
	}
	
	/*
	 * Draws a page to the screen.
	 * Currently only draws the "paper", not any of the content on it.
	 */
	public void drawPage(Graphics g, Page page) {
		Graphics2D g2d = (Graphics2D) g;
		
		//Makes the text anti-aliased
		g2d.setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		page.DrawPage((Graphics2D) g);
	}

}
