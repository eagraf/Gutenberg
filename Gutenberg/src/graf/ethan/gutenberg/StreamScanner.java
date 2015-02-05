package graf.ethan.gutenberg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class StreamScanner {
	private GutenbergScanner gScanner;
	private CrossReferenceScanner cScanner;
	private PdfScanner pScanner;
	private FileScanner fScanner;
	
	public StreamScanner(GutenbergScanner scanner) {
		this.gScanner = scanner;
		this.fScanner = this.gScanner.fileScanner;
		this.pScanner = this.gScanner.pdfScanner;
		this.cScanner = this.gScanner.crossScanner;
	}
	
	@SuppressWarnings("unchecked")
	public void scanStream(PdfObjectReference ref, Graphics2D g, Page page) {
		long pos = cScanner.getObjectPosition(ref);
		fScanner.setPosition(pos);
		pScanner.skipWhiteSpace();
		pScanner.scanNumeric();
		pScanner.skipWhiteSpace();
		pScanner.scanNumeric();
		pScanner.skipWhiteSpace();
		HashMap<String, Object> streamDictionary;
		
		Number length = 0;
		long startPos = 0;
		if(pScanner.scanKeyword() == 2) {
			streamDictionary = (HashMap<String, Object>) pScanner.scanNext();
			length = (Number) streamDictionary.get("Length");
		}
		else {
			streamDictionary = null;
		}
		if(pScanner.scanKeyword() == 4) {
			startPos = fScanner.getPosition();
			PdfStream stream = new PdfStream(startPos, length.longValue());
			
			ArrayList<Object> args = new ArrayList<Object>();
			while(fScanner.getPosition() < stream.endPos) {
				pScanner.skipWhiteSpace();
				Object next = pScanner.scanNext();
				if(next instanceof PdfOperator) {
					System.out.println(((PdfOperator) next) + ", " + args.toString());
					switch(((PdfOperator)next).id) {
						case 7:
						    scanText(g, page, stream);
						    break;
						case 11:
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
						case 12:
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
						case 13:
							ArrayList<Number> dashArray = (ArrayList<Number>) args.get(0);
							float phase = ((Number) args.get(1)).floatValue();
							
							page.state.dashArray = dashArray;
							page.state.phase = phase;
							break;
						case 25:
							page.state.colorStroking = new Color(
									((Number) args.get(0)).floatValue(),
									((Number) args.get(0)).floatValue(),
									((Number) args.get(0)).floatValue());
							break;
						case 26:
							page.state.colorNonStroking = new Color(
									((Number) args.get(0)).floatValue(),
									((Number) args.get(0)).floatValue(),
									((Number) args.get(0)).floatValue());
							break;
						case 31:
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
						case 32:
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
						case 36:
							Point2D m1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
									((Number) args.get(1)).doubleValue(), page.state);
							
							GeneralPath path = new GeneralPath();
							path.moveTo(m1.getX(), m1.getY());
							scanPath(g, page, stream, path);
							break;
						case 37:
							page.state.miterLimit = ((Number) args.get(0)).floatValue();
							break;
						case 42:
							Point2D re1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
									((Number) args.get(1)).doubleValue(), page.state);
							Point2D re2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
									((Number) args.get(3)).doubleValue(), page.state);
							
							Point2D zero = Transform.user_device(0,  0,  page.state);
							
							GeneralPath path1 = new GeneralPath();
							path1.moveTo(re1.getX(), re1.getY());
							path1.lineTo(re1.getX() + re2.getX() - zero.getX(), re1.getY());
							path1.lineTo(re1.getX() + re2.getX() - zero.getX(), re1.getY() + re2.getY() - zero.getY());
							path1.lineTo(re1.getX(), re1.getY() + re2.getY() - zero.getY());
							path1.closePath();
							scanPath(g, page, stream, path1);
							break;
						case 43:
							page.state.colorStroking = new Color(
									((Number) args.get(0)).floatValue(),
									((Number) args.get(1)).floatValue(),
									((Number) args.get(2)).floatValue());
							break;
						case 44:
							page.state.colorNonStroking = new Color(
									((Number) args.get(0)).floatValue(),
									((Number) args.get(1)).floatValue(),
									((Number) args.get(2)).floatValue());
							break;
						case 48:
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
						case 49:
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
						case 54:
							page.state.charSpace = (float) ((Number) args.get(0)).floatValue();
							break;
						case 57:
							page.state.font = (String) args.get(0);
							page.state.fontSize = ((Number) args.get(1)).intValue() * 4 / 3; //Point size is multiplied by 4/3 for correct size
							break;
						case 60:
							page.state.leading = (float) ((Number) args.get(0)).floatValue();
							break;
						case 62:
							page.state.renderMode = (int) ((Number) args.get(0)).intValue();
							break;
						case 63:
							page.state.textRise = (float) ((Number) args.get(0)).floatValue();
							break;
						case 64:
							page.state.wordSpace = (float) ((Number) args.get(0)).floatValue();
							break;
						case 65:
							page.state.textScale = (float) ((Number) args.get(0)).floatValue();
							break;
						case 67:
							page.state.lineWidth = ((Number) args.get(0)).floatValue();
							break;
					}
					args.clear();
				}
				else {
					args.add(next);
				}
			}
		}
	}
	
	private void scanPath(Graphics2D g, Page page, PdfStream stream, GeneralPath path) {
		boolean endPath = false;
		
		g.setColor(Color.BLACK);
		
		ArrayList<Object> args = new ArrayList<Object>();
		while(fScanner.getPosition() < stream.endPos && endPath == false) {
			pScanner.skipWhiteSpace();
			Object next = pScanner.scanNext();
			if(next instanceof PdfOperator) {
				System.out.println(((PdfOperator) next) + ", " + args.toString());
				switch(((PdfOperator)next).id) {
					case 0:
						path.closePath();
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);				
						gScanner.gutenbergDrawer.fillPath(g, page, path);
						gScanner.gutenbergDrawer.drawPath(g, page, path);
						endPath = true;
						break;
					case 1:
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);				
						gScanner.gutenbergDrawer.fillPath(g, page, path);
						gScanner.gutenbergDrawer.drawPath(g, page, path);
						endPath = true;
						break;
					case 2:
						path.closePath();
						path.setWindingRule(GeneralPath.WIND_EVEN_ODD);				
						gScanner.gutenbergDrawer.fillPath(g, page, path);
						gScanner.gutenbergDrawer.drawPath(g, page, path);
						endPath = true;
						break;
					case 3:
						path.setWindingRule(GeneralPath.WIND_EVEN_ODD);				
						gScanner.gutenbergDrawer.fillPath(g, page, path);
						gScanner.gutenbergDrawer.drawPath(g, page, path);
						endPath = true;
						break;
					case 9:
						Point2D c1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D c2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						Point2D c3 = Transform.user_device(((Number) args.get(4)).doubleValue(),
								((Number) args.get(5)).doubleValue(), page.state);	

						path.curveTo(c1.getX(), c1.getY(), c2.getX(), c2.getY(), c3.getX(), c3.getY());
						break;
					case 11:
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
					case 12:
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
					case 13:
						@SuppressWarnings("unchecked")
						ArrayList<Number> dashArray = (ArrayList<Number>) args.get(0);
						float phase = ((Number) args.get(1)).floatValue();
						
						page.state.dashArray = dashArray;
						page.state.phase = phase;
						break;
					case 22:
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);
						gScanner.gutenbergDrawer.fillPath(g, page, path);
						endPath = true;
						break;
					case 23:
						path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
						gScanner.gutenbergDrawer.fillPath(g, page, path);
						endPath = true;
						break;
					case 24:
						path.setWindingRule(GeneralPath.WIND_NON_ZERO);
						gScanner.gutenbergDrawer.fillPath(g, page, path);
						endPath = true;
						break;
					case 25:
						page.state.colorStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue());
						break;
					case 26:
						page.state.colorNonStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue());
						break;
					case 28:
						path.closePath();
						break;
					case 31:
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
					case 32:
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
					case 35:
						Point2D l1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						
						path.lineTo(l1.getX(), l1.getY()); 
						break;
					case 36:
						Point2D m1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						
						path.moveTo(m1.getX(), m1.getY());
						break;
					case 37:
						page.state.miterLimit = ((Number) args.get(0)).floatValue();
						break;
					case 39:
						endPath = true;
						break;
					case 42:
						Point2D re1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D re2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						
						Point2D zero = Transform.user_device(0,  0,  page.state);
						
						GeneralPath path1 = new GeneralPath();
						path1.moveTo(re1.getX(), re1.getY());
						path1.lineTo(re1.getX() + re2.getX() - zero.getX(), re1.getY());
						path1.lineTo(re1.getX() + re2.getX() - zero.getX(), re1.getY() + re2.getY() - zero.getY());
						path1.lineTo(re1.getX(), re1.getY() + re2.getY() - zero.getY());
						path.closePath();
						break;
					case 43:
						page.state.colorStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(1)).floatValue(),
								((Number) args.get(2)).floatValue());
						break;
					case 44:
						page.state.colorNonStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(1)).floatValue(),
								((Number) args.get(2)).floatValue());
						break;
					case 46:
						path.closePath();
						gScanner.gutenbergDrawer.drawPath(g, page, path);
						endPath = true;
						break;
					case 47:
						gScanner.gutenbergDrawer.drawPath(g, page, path);
						endPath = true;
						break;
					case 48:
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
					case 49:
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
					case 66:
						Point2D v1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D v2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						
						path.curveTo((double) path.getCurrentPoint().getX(),
								(double) path.getCurrentPoint().getY(), v1.getX(), v1.getY(), v2.getX(), v2.getY());
						break;
					case 67:
						page.state.lineWidth = ((Number) args.get(0)).floatValue();
						break;
					case 70:
						Point2D y1 = Transform.user_device(((Number) args.get(0)).doubleValue(),
								((Number) args.get(1)).doubleValue(), page.state);
						Point2D y2 = Transform.user_device(((Number) args.get(2)).doubleValue(),
								((Number) args.get(3)).doubleValue(), page.state);
						
						path.curveTo(y1.getX(), y1.getY(), y2.getX(), y2.getY(), y2.getX(), y2.getY());
						break;
				}
				args.clear();
			}
			else {
				args.add(next);
			}
		}
	}
	
	private void scanText(Graphics2D g, Page page, PdfStream stream) {
		boolean endText = false;
		int x = 0;
		int y = 0;
		int size = 12;
		String font = "";
		String text = "";
		Color color = Color.BLACK;
		
		ArrayList<Object> args = new ArrayList<Object>();
		while(fScanner.getPosition() < stream.endPos && endText == false) {
			pScanner.skipWhiteSpace();
			Object next = pScanner.scanNext();
			if(next instanceof PdfOperator) {
				System.out.println(((PdfOperator) next) + ", " + args.toString());
				switch(((PdfOperator)next).id) {
				case 11:
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
				case 12:
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
					case 20:
						gScanner.gutenbergDrawer.drawText(g, page, text, x, y, size, font, color);
						endText = true;
						break;
					case 25:
						page.state.colorStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue());
						break;
					case 26:
						page.state.colorNonStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue(),
								((Number) args.get(0)).floatValue());
						break;
					case 43:
						page.state.colorStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(1)).floatValue(),
								((Number) args.get(2)).floatValue());
						break;
					case 44:
						page.state.colorNonStroking = new Color(
								((Number) args.get(0)).floatValue(),
								((Number) args.get(1)).floatValue(),
								((Number) args.get(2)).floatValue());
						break;
					case 48:
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
					case 49:
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
					case 55:
						x = ((Number) args.get(0)).intValue();
						y = ((Number) args.get(1)).intValue();
						break;
					case 57:
						font = (String) args.get(0);
						size = ((Number) args.get(1)).intValue() * 4 / 3; //Point size is multiplied by 4/3 for correct size
						break;
					case 58:
						text = (String) args.get(0);
						break;
					case 60:
						page.state.leading = (float) ((Number) args.get(0)).floatValue();
						break;
					case 62:
						page.state.renderMode = (int) ((Number) args.get(0)).intValue();
						break;
					case 63:
						page.state.textRise = (float) ((Number) args.get(0)).floatValue();
						break;
					case 64:
						page.state.wordSpace = (float) ((Number) args.get(0)).floatValue();
						break;
					case 65:
						page.state.textScale = (float) ((Number) args.get(0)).floatValue();
						break;
					case 67:
						page.state.lineWidth = ((Number) args.get(0)).floatValue();
						break;
				}
				args.clear();
			}
			else {
				args.add(next);
			}
		}
	}
}
