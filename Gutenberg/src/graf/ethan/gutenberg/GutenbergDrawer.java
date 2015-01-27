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
	private float scale = 1;

	public GutenbergDrawer(GutenbergScanner scanner) {
		this.scanner = scanner;
	}
	
	public float getScale() {
		return scale;
	}
	
	public void setScale(float factor) {
		this.scale = factor;
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
		g2d.setRenderingHint(
				RenderingHints.KEY_FRACTIONALMETRICS, 
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		
		System.out.println(g2d.getFontMetrics().getFontRenderContext().isTransformed());
		
		g2d.setColor(Color.WHITE);
		g2d.fillRect(page.x, page.y, (int) (page.WIDTH * scale), (int) (page.HEIGHT * scale));
		
		scanner.streamScanner.scanStream(page.contents, g2d, page);
	}
	
	public void drawText(Graphics g, Page page, String text, int x, int y, int size, String fontName, Color color) {
		Font font = page.fonts.get(fontName).getFont(Font.PLAIN, (int) (size * scale));
		System.out.println(font.getSize() + ", " + font.getSize2D());
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, page.x + (int) (x * scale), page.y + (int) (page.HEIGHT * scale) - (int) (y * scale));
	}
}
