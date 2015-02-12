package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/*
 * Class responsible for the drawing of the PDF onto the screen.
 */
public class GutenbergDrawer {
	public final int RESOLUTION;
	
	public GutenbergScanner scanner;
	
	public Matrix scaleMatrix;
	private double scale = 1;
	
	public Graphics2D g;

	public GutenbergDrawer(GutenbergScanner scanner) {
		this.scanner = scanner;
		
		RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution();
		System.out.println("Resolution: " + RESOLUTION);
		
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
	public void drawPage(Graphics graphics, Page page) {
		this.g = (Graphics2D) graphics;
		
		//Makes the text anti-aliased
		g.setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(
				RenderingHints.KEY_FRACTIONALMETRICS, 
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		
		g.setColor(Color.WHITE);
		g.fillRect(page.x, page.y, page.dWidth, page.dHeight);
		
		scanner.streamScanner.setStream(page.contents);
		
		operate(page);
	}
	
	public void drawPath(Page page, GeneralPath path) {
		g.setColor(page.state.colorStroking);
		
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
			g.setStroke(new BasicStroke(Transform.scale(page.state.lineWidth, page.state), 
					Transform.scale(page.state.lineCap, page.state), 
					Transform.scale(page.state.lineJoin, page.state), 
					Transform.scale(page.state.miterLimit, page.state)));
		}
		else {
			g.setStroke(new BasicStroke(Transform.scale(page.state.lineWidth, page.state), 
					Transform.scale(page.state.lineCap, page.state), 
					Transform.scale(page.state.lineJoin, page.state), 
					Transform.scale(page.state.miterLimit, page.state),
					dashArray, Transform.scale(page.state.phase, page.state)));
		}		
		
		g.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.draw(path);
	}
	
	public void fillPath(Page page, GeneralPath path) {
		g.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(page.state.colorNonStroking);
		g.fill(path);
	}
	
	public void drawText(Page page, String text) {
		//Determine the font
		Font font;
		if(page.state.font != null) {
			font = page.fonts.get(page.state.font).getFont(Font.PLAIN, (int) (page.state.fontSize * scale));
		}
		else {
			font = new Font("Times New Roman", Font.PLAIN, 12);
		}
		
		FontRenderContext frc = g.getFontRenderContext();
		GlyphVector gv = font.createGlyphVector(frc, text);
		int length = gv.getNumGlyphs();
		
		GeneralPath path;
		GlyphMetrics metrics;
		
		for(int i = 0; i < length; i ++) {
			Point2D p = gv.getGlyphPosition(i);
			if(page.state.cache.has(font, text.charAt(i), page.state.textScale)) {
				path = page.state.cache.get(font, text.charAt(i), page.state.textScale).path;
				metrics = page.state.cache.get(font, text.charAt(i), page.state.textScale).metrics;
			}
			else {
				path = (GeneralPath) gv.getGlyphOutline(i);
				AffineTransform at = new AffineTransform();
				at.setToTranslation((double) -p.getX(), -p.getY());
				path = new GeneralPath(at.createTransformedShape(path));
				metrics = gv.getGlyphMetrics(i);
				page.state.cache.put(font, text.charAt(i), page.state.textScale, new Glyph(path, metrics));
			}
			
			Matrix temp = page.state.getTextRenderingMatrix().multiply(page.state.textMatrix);
			Matrix loc = temp.multiply(page.state.ctm);
			
			AffineTransform t = new AffineTransform();
			
			t.setTransform(loc.getScaleX(),
					loc.getRotateX(),
					loc.getRotateY(),
					-loc.getScaleY(),
					loc.getTranslateX(),
					loc.getTranslateY());
			path = new GeneralPath(t.createTransformedShape(path));
			switch(page.state.renderMode) {
				case 0:
					fillPath(page, path);
					break;
				case 1:
					drawPath(page, path);
					break;
				case 2:
					drawPath(page, path);
					fillPath(page, path);
					break;
				case 3:
					//Invisible
					break;
				case 4:
					//Clipping rendering modes ... will  implement later
					break;
				case 5:
					break;
				case 6:
					break;
				case 7:
					break;
			}
			
			
			if(text.charAt(i) != ' ') {
				page.state.incrementText(((metrics.getAdvanceX() * 3/4) + page.state.charSpace) * loc.getScaleX(),
						metrics.getAdvanceY() * 3/4);
			}
			else {
				page.state.incrementText(((metrics.getAdvanceX() * 3/4) + page.state.wordSpace) * loc.getScaleX(),
						metrics.getAdvanceY() * 3/4);

			}
		}
	}
	
	public void operate(Page page) {
		GeneralPath path = new GeneralPath();
		PdfOperation next = scanner.streamScanner.nextOperation();
		while(next != null) {
			System.out.println(next);
			if(next != null) {
				ArrayList<Object> args = next.args;
				switch(next.operator.id) {
					case PdfOperator.Operator_B:
						//b: Close the path, fill the path, and stroke it, using the non-zero winding rule
						path.closePath();
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);				
						fillPath(page, path);
						drawPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.OperatorB: 
						//B: Fill the path, and stroke it, using the non-zero winding rule
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);				
						fillPath(page, path);
						drawPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.Operator_B_Star:
						//b*: Close the path, fill the path, and stroke it, using the even-odd winding rule
						path.closePath();
						path.setWindingRule(GeneralPath.WIND_EVEN_ODD);				
						fillPath(page, path);
						drawPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.OperatorB_Star: 
						//B*: Fill the path, and stroke it, using the even-odd winding rule
						path.setWindingRule(GeneralPath.WIND_EVEN_ODD);				
						fillPath(page, path);
						drawPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.OperatorBDC:
					case PdfOperator.OperatorBI:
					case PdfOperator.OperatorBMC:
					case PdfOperator.OperatorBT:
						//BT: Create a new text object
						//No operation
						break;
					case PdfOperator.OperatorBX:
					case PdfOperator.Operator_C:
						//c: Append a curve to the current path.
						Point2D c1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D c2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						Point2D c3 = Transform.user_device(((Number) args.get(4)).doubleValue(),
								((Number) args.get(5)).doubleValue(), page.state);	
	
						path.curveTo(c1.getX(), c1.getY(), c2.getX(), c2.getY(), c3.getX(), c3.getY());
						break;
					case PdfOperator.Operator_CM:
					case PdfOperator.OperatorCS:
						//CS: Set the Color Space for stroking operations
						//A lot of the color spaces are not implemented
						String space11 = (String) args.get(0);
						page.state.colorSpaceStroking = space11;
						switch(space11) {
							case "DeviceGray":
								page.state.colorStroking = Color.BLACK;
								break;
							case "DeviceRGB":
								page.state.colorStroking = Color.BLACK;
								break;
							case "DeviceCMYK":
								float[] default_cmyk = {0.0f, 0.0f, 0.0f, 1.0f};
								page.state.colorStroking = new Color(
										ColorSpace.getInstance(ColorSpace.TYPE_CMYK),
										default_cmyk, 1.0f);
								break;
							default:
								page.state.colorSpaceStroking = (String) args.get(0);
								break;
						}
						break;
					case PdfOperator.Operator_CS:
						//cs: Set the Color Space for non-stroking operations
						//A lot of the color spaces are not implemented
						String space12 = (String) args.get(0);
						page.state.colorSpaceNonStroking = space12;
						switch(space12) {
							case "DeviceGray":
								page.state.colorNonStroking = Color.BLACK;
								break;
							case "DeviceRGB":
								page.state.colorNonStroking = Color.BLACK;
								break;
							case "DeviceCMYK":
								float[] default_cmyk = {0.0f, 0.0f, 0.0f, 1.0f};
								page.state.colorNonStroking = new Color(
										ColorSpace.getInstance(ColorSpace.TYPE_CMYK),
										default_cmyk, 1.0f);
								break;
							default:
								page.state.colorSpaceNonStroking = (String) args.get(0);
								break;
						}
						break;
					case PdfOperator.Operator_D: 
						//d: Sets the dashing parameters for stroking operations
						@SuppressWarnings("unchecked")
						ArrayList<Number> dashArray = (ArrayList<Number>) args.get(0);
						float phase = ((Number) args.get(1)).floatValue();
						
						page.state.dashArray = dashArray;
						page.state.phase = phase;
						break;
					case PdfOperator.Operator_D0:
					case PdfOperator.Operator_D1:
					case PdfOperator.OperatorDO: 
					case PdfOperator.OperatorDP: 
					case PdfOperator.OperatorEI: 
					case PdfOperator.OperatorEMC:
					case PdfOperator.OperatorET: 
						//ET: End the text object.
						//No operation
						break;
					case PdfOperator.OperatorEX: 
					case PdfOperator.Operator_F: 
						//f: Fill the path using the non-zero winding rule.
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);
						fillPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.OperatorF:
						//F: Fill the path using the non-zero winding rule (Obsolete).
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);
						fillPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.Operator_F_Star:
						//f: Fill the path using the even-odd winding rule.
						path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
						fillPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.OperatorG:
						//G: Set the stroking color for the DeviceGray color space
						page.state.colorStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue());
						break;
					case PdfOperator.Operator_G: 
						//g: Set the non-stroking color for the DeviceGray colo space
						page.state.colorNonStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue());
						break;
					case PdfOperator.Operator_GS:
					case PdfOperator.Operator_H: 
						//h: Close the current path
						path.closePath();
						break;
					case PdfOperator.Operator_I: 
					case PdfOperator.OperatorID: 
					case PdfOperator.Operator_J: 
						//j: Set the join parameter for the text space ... Determines how angles in lines are rendered
						switch(((Number) args.get(0)).intValue()) {
							case 0:
								page.state.lineJoin = BasicStroke.JOIN_MITER;
								break;
							case 1:
								page.state.lineJoin = BasicStroke.JOIN_ROUND;
								break;
							case 2:
								page.state.lineJoin = BasicStroke.JOIN_BEVEL;
								break;
						}
						break;
					case PdfOperator.OperatorJ:
						//J: Set the capping parameters for the text state ... Determine how the ends of lines are rendered
						switch(((Number) args.get(0)).intValue()) {
							case 0:
								page.state.lineCap = BasicStroke.CAP_BUTT;
								break;
							case 1:
								page.state.lineCap = BasicStroke.CAP_ROUND;
								break;
							case 2:
								page.state.lineCap = BasicStroke.CAP_SQUARE;
								break;
						}
						break;
					case PdfOperator.OperatorK:
					case PdfOperator.Operator_K: 
					case PdfOperator.Operator_L:
						//l: Append a line to the current path.
						Point2D l1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						System.out.println(next.operator.name);
						path.lineTo(l1.getX(), l1.getY()); 
						break;
					case PdfOperator.Operator_M: 
						//m: MoveTo ... Starts a new path at the specified location
						Point2D m1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						path.moveTo(m1.getX(), m1.getY());
						break;
					case PdfOperator.OperatorM:
						//M: Sets the limit to how sharp angles can be and how they are rendered
						page.state.miterLimit = ((Number) args.get(0)).floatValue();
						break;
					case PdfOperator.OperatorMP: 
					case PdfOperator.Operator_N:
						//n: End the current path.
						path = new GeneralPath();
						break;
					case PdfOperator.Operator_Q:
					case PdfOperator.OperatorQ:
					case PdfOperator.Operator_RE:
						//re: Create a new rectangular path ... equivalent to a MoveTo and three LineTo's
						Point2D re1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D re2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						
						Point2D zero = Transform.user_device(0,  0,  page.state);
						
						path.moveTo(re1.getX(), re1.getY());
						path.lineTo(re1.getX() + re2.getX() - zero.getX(), re1.getY());
						path.lineTo(re1.getX() + re2.getX() - zero.getX(), re1.getY() + re2.getY() - zero.getY());
						path.lineTo(re1.getX(), re1.getY() + re2.getY() - zero.getY());
						path.closePath();
			
						break;
					case PdfOperator.OperatorRG:
						//RG: Set the stroking color in DeviceRGB color space
						page.state.colorStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(1)).floatValue(),
								((Number) args.get(2)).floatValue());
						break;
					case PdfOperator.Operator_RG:
						//rg: Set the non-stroking color in DeviceRGB color space
						page.state.colorNonStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(1)).floatValue(),
								((Number) args.get(2)).floatValue());
						break;
					case PdfOperator.Operator_RI:
					case PdfOperator.Operator_S: 
						//s: Close the path and stroke it.
						path.closePath();
						drawPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.OperatorS:
						//S: Stroke the path.
						drawPath(page, path);
						path = new GeneralPath();
						break;
					case PdfOperator.OperatorSC: 
						//SC: Set the stroking color space
						switch(page.state.colorSpaceStroking) {
							case "DeviceRGB":
								page.state.colorStroking = new Color(
										((Number) args.get(0)).floatValue(),
										((Number) args.get(1)).floatValue(),
										((Number) args.get(2)).floatValue());
								break;
							case "DeviceCMYK":
								float[] values_cmyk = {((Number) args.get(0)).floatValue(),
										((Number) args.get(1)).floatValue(),
										((Number) args.get(2)).floatValue(),
										((Number) args.get(3)).floatValue()
								};
								page.state.colorStroking = new Color(
										ColorSpace.getInstance(ColorSpace.TYPE_CMYK),
										values_cmyk, 0.0f);
								break;
							case "DeviceGray":
								page.state.colorStroking = new Color(((Number) args.get(0)).floatValue(),
										((Number) args.get(0)).floatValue(),
										((Number) args.get(0)).floatValue());
								break;
						}
						break;
					case PdfOperator.Operator_SC:
						//sc: Set the non-stroking color space
						switch(page.state.colorSpaceNonStroking) {
							case "DeviceRGB":
								page.state.colorNonStroking = new Color(
										((Number) args.get(0)).floatValue(),
										((Number) args.get(1)).floatValue(),
										((Number) args.get(2)).floatValue());
								break;
							case "DeviceCMYK":
								float[] values_cmyk = {((Number) args.get(0)).floatValue(),
										((Number) args.get(1)).floatValue(),
										((Number) args.get(2)).floatValue(),
										((Number) args.get(3)).floatValue()
								};
								page.state.colorNonStroking = new Color(
										ColorSpace.getInstance(ColorSpace.TYPE_CMYK),
										values_cmyk, 0.0f);
								break;
							case "DeviceGray":
								page.state.colorNonStroking = new Color(((Number) args.get(0)).floatValue(),
										((Number) args.get(0)).floatValue(),
										((Number) args.get(0)).floatValue());
								break;
						}
						break;
					case PdfOperator.OperatorSCN:
					case PdfOperator.Operator_SCN:
					case PdfOperator.Operator_SH:
					case PdfOperator.OperatorT_Star:
						//T*: Move to the next line.
						page.state.setTextStart(1, 0, 0, 1, 0, page.state.leading);
						break;
					case PdfOperator.Operator_TC:
						//Tc: Set the additional space between characters
						page.state.charSpace = (float) ((Number) args.get(0)).floatValue();
						break;
					case PdfOperator.Operator_TD:
						//Td: Set the location to where text will be drawn
						page.state.setTextStart(1, 0, 0, 1, 
								((Number) args.get(0)).floatValue(), 
								((Number) args.get(1)).floatValue());
						break;
					case PdfOperator.OperatorTD:
						//TD: Set the leading and move down a line
						page.state.leading = (float) ((Number) args.get(1)).floatValue();
						page.state.setTextStart(1, 0, 0, 1, 
								((Number) args.get(0)).floatValue(), 
								((Number) args.get(1)).floatValue());
						break;
					case PdfOperator.Operator_TF:
						//Tf: Set the font and font size for text
						page.state.font = (String) args.get(0);
						page.state.fontSize = Transform.scale(((Number) args.get(1)).intValue(), page.state);
						break;
					case PdfOperator.Operator_TJ:
						//Tj: Draw the given text
						drawText(page, (String) args.get(0));
						break;
					case PdfOperator.OperatorTJ: 
						//TJ: Draw text with specified spacing.
						@SuppressWarnings("unchecked")
						ArrayList<Object> textArray = (ArrayList<Object>) args.get(0);
						for(int i = 0; i < textArray.size(); i ++) {
							if(textArray.get(i).getClass().isAssignableFrom(Number.class)) {
								page.state.incrementText(-((Number) textArray.get(i)).doubleValue(), 0); 
							}
							else {
								drawText(page, (String) textArray.get(i));
							}
						}
						break;
					case PdfOperator.OperatorTL: 
						//Tl: Set the leading for text (The distance between lines)
						page.state.leading = (float) ((Number) args.get(0)).floatValue();
						break;
					case PdfOperator.Operator_TM:
						//Tm: Sets the origin of text space to the location specified in a matrix
						page.state.setTextStart(((Number) args.get(0)).floatValue(),
								((Number) args.get(1)).floatValue(), 
								((Number) args.get(2)).floatValue(), 
								((Number) args.get(3)).floatValue(), 
								((Number) args.get(4)).floatValue(), 
								((Number) args.get(5)).floatValue());
						break;
					case PdfOperator.Operator_TR:
						//Tr: Set the rendering mode of text
						page.state.renderMode = (int) ((Number) args.get(0)).intValue();
						break;
					case PdfOperator.Operator_TS:
						//Ts: Set the rise parameter for text (The elevation above the baseline)
						page.state.textRise = (float) ((Number) args.get(0)).floatValue();
						break;
					case PdfOperator.Operator_TW:
						//Tw: Set the spacing between words
						page.state.wordSpace = (float) ((Number) args.get(0)).floatValue();
						break;
					case PdfOperator.Operator_TZ:
						//Tz: Set the horizontal scaling factor for text
						page.state.textScale = (float) ((Number) args.get(0)).floatValue()/100;
						break;
					case PdfOperator.Operator_V:
						//v: Append a curve to the current path, using the current point as a control point.
						Point2D v1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D v2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						
						path.curveTo((double) path.getCurrentPoint().getX(),
								(double) path.getCurrentPoint().getY(), v1.getX(), v1.getY(), v2.getX(), v2.getY());
						break;
					case PdfOperator.Operator_W:
						//w: Set the line width for paths
						page.state.lineWidth = ((Number) args.get(0)).floatValue();
						break;
					case PdfOperator.OperatorW:
					case PdfOperator.OperatorW_Star: 
					case PdfOperator.Operator_Y:
						//y: Append a curve to the current path, using the end point as a control point.
						Point2D y1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D y2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						
						path.curveTo(y1.getX(), y1.getY(), y2.getX(), y2.getY(), y2.getX(), y2.getY());
						break;
					case PdfOperator.Operator_Single:
						//': Move text down a line and draw it (using the leading).
						page.state.setTextStart(1, 0, 0, 1, 0, page.state.leading);
						drawText(page, (String) args.get(0));
						break;
					case PdfOperator.Operator_Double:
						//": Move the text down a line, specify spacings and draw text.
						page.state.wordSpace = ((Number) args.get(0)).floatValue();
						page.state.charSpace = ((Number) args.get(1)).floatValue();
						page.state.setTextStart(1, 0, 0, 1, 0, page.state.leading);
						drawText(page, (String) args.get(2));
						break;
				}
			}
			next = scanner.streamScanner.nextOperation();
		}		
	}
}