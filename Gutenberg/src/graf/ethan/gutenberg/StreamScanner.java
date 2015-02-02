package graf.ethan.gutenberg;

import graf.ethan.gropius.Path;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
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
		
		long length = 0;
		long startPos = 0;
		if(pScanner.scanKeyword() == 2) {
			streamDictionary = (HashMap<String, Object>) pScanner.scanNext();
			length = (long) streamDictionary.get("Length");
		}
		else {
			streamDictionary = null;
		}
		if(pScanner.scanKeyword() == 4) {
			startPos = fScanner.getPosition();
			PdfStream stream = new PdfStream(startPos, length);
			
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
						case 36:
							GeneralPath path = new GeneralPath();
							path.moveTo(((Long) args.get(0)).floatValue(), ((Long) args.get(1)).floatValue());
							scanPath(g, page, stream, path);
							break;
						case 42:
							GeneralPath path1 = new GeneralPath();
							path1.moveTo(((Long) args.get(0)).floatValue(), ((Long) args.get(1)).floatValue());
							path1.lineTo(((Long) args.get(0)).floatValue() + ((Long) args.get(2)).floatValue(), ((Long) args.get(1)).floatValue());
							path1.lineTo(((Long) args.get(0)).floatValue() + ((Long) args.get(2)).floatValue(), ((Long) args.get(1)).floatValue() + ((Long) args.get(3)).floatValue());
							path1.lineTo(((Long) args.get(0)).floatValue(), ((Long) args.get(1)).floatValue() + ((Long) args.get(3)).floatValue());
							path1.closePath();
							scanPath(g, page, stream, path1);
							break;
						case 43:
							float red = ((Number) args.get(0)).floatValue();
							float green = ((Number) args.get(1)).floatValue();
							float blue = ((Number) args.get(2)).floatValue();
							
							int intRed = (int) (255f * red);
							int intGreen = (int) (255f * green);
							int intBlue = (int) (255f * blue);
							System.out.println(red + " " + green + " " + blue);
							page.state.colorStroking = new Color(intRed, intGreen, intBlue);
							break;
						case 44:
							float red1 = ((Number) args.get(0)).floatValue();
							float green1 = ((Number) args.get(1)).floatValue();
							float blue1 = ((Number) args.get(2)).floatValue();
							
							int intRed1 = (int) (255f * red1);
							int intGreen1 = (int) (255f * green1);
							int intBlue1 = (int) (255f * blue1);
							
							page.state.colorNonStroking = new Color(intRed1, intGreen1, intBlue1);
							break;
						case 54:
							page.state.charSpace = (float) ((Long) args.get(0)).floatValue();
							break;
						case 57:
							page.state.font = (String) args.get(0);
							page.state.fontSize = ((Long) args.get(1)).intValue() * 4 / 3; //Point size is multiplied by 4/3 for correct size
							break;
						case 60:
							page.state.leading = (float) ((Long) args.get(0)).floatValue();
							break;
						case 62:
							page.state.renderMode = (int) ((Long) args.get(0)).intValue();
							break;
						case 63:
							page.state.textRise = (float) ((Long) args.get(0)).floatValue();
							break;
						case 64:
							page.state.wordSpace = (float) ((Long) args.get(0)).floatValue();
							break;
						case 65:
							page.state.scale = (float) ((Long) args.get(0)).floatValue();
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
						path.curveTo(((Long) args.get(0)).doubleValue(),((Long) args.get(1)).doubleValue(), ((Long) args.get(2)).doubleValue(),((Long) args.get(3)).doubleValue(), ((Long) args.get(4)).doubleValue(), ((Long) args.get(5)).doubleValue());
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
					case 28:
						path.closePath();
						break;
					case 35:
						path.lineTo(((Long) args.get(0)).floatValue(), ((Long) args.get(1)).floatValue()); 
						break;
					case 36:
						path.moveTo(((Long) args.get(0)).floatValue(), ((Long) args.get(1)).floatValue());
						break;
					case 39:
						endPath = true;
						break;
					case 42:
						path.moveTo(((Long) args.get(0)).floatValue(), ((Long) args.get(1)).floatValue());
						path.lineTo(((Long) args.get(0)).floatValue() + ((Long) args.get(2)).floatValue(), ((Long) args.get(1)).floatValue());
						path.lineTo(((Long) args.get(0)).floatValue() + ((Long) args.get(2)).floatValue(), ((Long) args.get(1)).floatValue() + ((Long) args.get(3)).floatValue());
						path.lineTo(((Long) args.get(0)).floatValue(), ((Long) args.get(1)).floatValue() + ((Long) args.get(3)).floatValue());
						path.closePath();
						break;
					case 43:
						float red = ((Number) args.get(0)).floatValue();
						float green = ((Number) args.get(1)).floatValue();
						float blue = ((Number) args.get(2)).floatValue();
						
						int intRed = (int) (255f * red);
						int intGreen = (int) (255f * green);
						int intBlue = (int) (255f * blue);
						System.out.println(red + " " + green + " " + blue);
						page.state.colorStroking = new Color(intRed, intGreen, intBlue);
						break;
					case 44:
						float red1 = ((Number) args.get(0)).floatValue();
						float green1 = ((Number) args.get(1)).floatValue();
						float blue1 = ((Number) args.get(2)).floatValue();
						
						int intRed1 = (int) (255f * red1);
						int intGreen1 = (int) (255f * green1);
						int intBlue1 = (int) (255f * blue1);
						
						page.state.colorNonStroking = new Color(intRed1, intGreen1, intBlue1);
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
					case 66:
						//This probably doesn't work correctly
						path.curveTo((double) path.getCurrentPoint().getX(), (double) path.getCurrentPoint().getY(), ((Long) args.get(0)).doubleValue(), ((Long) args.get(1)).doubleValue(), ((Long) args.get(2)).doubleValue(), ((Long) args.get(3)).doubleValue());
						break;
					case 70:
						path.curveTo(((Long) args.get(0)).doubleValue(),((Long) args.get(1)).doubleValue(), ((Long) args.get(2)).doubleValue(),((Long) args.get(3)).doubleValue(), ((Long) args.get(2)).doubleValue(), ((Long) args.get(3)).doubleValue());
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
					case 20:
						gScanner.gutenbergDrawer.drawText(g, page, text, x, y, size, font, color);
						endText = true;
						break;
					case 43:
						float red = ((Number) args.get(0)).floatValue();
						float green = ((Number) args.get(1)).floatValue();
						float blue = ((Number) args.get(2)).floatValue();
						
						int intRed = (int) (255f * red);
						int intGreen = (int) (255f * green);
						int intBlue = (int) (255f * blue);
						System.out.println(red + " " + green + " " + blue);
						page.state.colorStroking = new Color(intRed, intGreen, intBlue);
						break;
					case 44:
						float red1 = ((Number) args.get(0)).floatValue();
						float green1 = ((Number) args.get(1)).floatValue();
						float blue1 = ((Number) args.get(2)).floatValue();
						
						int intRed1 = (int) (255f * red1);
						int intGreen1 = (int) (255f * green1);
						int intBlue1 = (int) (255f * blue1);
						
						page.state.colorNonStroking = new Color(intRed1, intGreen1, intBlue1);
						break;
					case 55:
						x = ((Long) args.get(0)).intValue();
						y = ((Long) args.get(1)).intValue();
						break;
					case 57:
						font = (String) args.get(0);
						size = ((Long) args.get(1)).intValue() * 4 / 3; //Point size is multiplied by 4/3 for correct size
						break;
					case 58:
						text = (String) args.get(0);
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
