package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.GeneralPath;

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
	
	public void drawPath(Graphics2D g, Page page, GeneralPath path) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(page.state.colorStroking);
		
		boolean dashed = false;
		float[] dashArray = new float[page.state.dashArray.size()];
		for(int i = 0; i < page.state.dashArray.size(); i ++) {
			float val = ((Number) page.state.dashArray.get(i)).floatValue();
			dashArray[i] = Transform.scale(val, page.state);
			if(val != 0) {
				dashed = true;
			}
		}
		
		if(page.state.dashArray.isEmpty() == true || !dashed){
			g2d.setStroke(new BasicStroke(Transform.scale(page.state.lineWidth, page.state), 
					Transform.scale(page.state.lineCap, page.state), 
					Transform.scale(page.state.lineJoin, page.state), 
					Transform.scale(page.state.miterLimit, page.state)));
		}
		else {
			g2d.setStroke(new BasicStroke(Transform.scale(page.state.lineWidth, page.state), 
					Transform.scale(page.state.lineCap, page.state), 
					Transform.scale(page.state.lineJoin, page.state), 
					Transform.scale(page.state.miterLimit, page.state),
					dashArray, Transform.scale(page.state.phase, page.state)));
		}		
		
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.draw(path);
	}
	
	public void fillPath(Graphics2D g, Page page, GeneralPath path) {
		g.setColor(page.state.colorNonStroking);
		g.fill(path);
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
