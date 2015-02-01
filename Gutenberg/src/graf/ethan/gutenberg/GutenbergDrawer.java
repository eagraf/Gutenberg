package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;

/*
 * Class responsible for the drawing of the PDF onto the screen.
 */
public class GutenbergDrawer {
	public final int RESOLUTION;
	
	public GutenbergScanner scanner;
	
	public Matrix scaleMatrix;
	private double scale = 1;

	public GutenbergDrawer(GutenbergScanner scanner) {
		this.scanner = scanner;
		
		RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution();
		System.out.println("Resolution = " + RESOLUTION);
		
		scaleMatrix = new Matrix(3, 3, 0);
		scaleMatrix.set(0,  0,  scale);
		scaleMatrix.set(1, 1, scale);
	}
	
	public double getScale() {
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
		
		g2d.setColor(Color.WHITE);
		g2d.fillRect(page.x, page.y, page.dWidth, page.dHeight);
		
		scanner.streamScanner.scanStream(page.contents, g2d, page);
	}
	
	public void drawText(Graphics g, Page page, String text, int x, int y, int size, String fontName, Color color) {
		//Determine the font
		Font font;
		if(fontName == "" && page.state.font != null) {
			font = page.fonts.get(page.state.font).getFont(Font.PLAIN, (int) (page.state.fontSize * scale));
		}
		else if(fontName != "") {
			font = page.fonts.get(fontName).getFont(Font.PLAIN, (int) (size * scale));
		}
		else {
			font = new Font("Times New Roman", Font.PLAIN, 12);
		} 
		
		double[][] tl = {{(double) x}, {(double) y}, {1f}};
		Matrix tlc = new Matrix(tl);
		Matrix m = tlc.multiply(page.state.ctm);
		
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, (int) m.get(0, 0), (int) m.get(1, 0));
	}
}
