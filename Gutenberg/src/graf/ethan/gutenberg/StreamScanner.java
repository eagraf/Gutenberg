package graf.ethan.gutenberg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
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
			startPos = fScanner.getPosition();
		}
		else {
			streamDictionary = null;
		}
		if(pScanner.scanKeyword() == 4) {
			ArrayList<Object> argStack = new ArrayList();
			while(fScanner.getPosition() < startPos + length) {
				pScanner.skipWhiteSpace();
				Object next = pScanner.scanNext();
				if(next instanceof PdfOperator) {
					System.out.println(((PdfOperator) next) + ", " + argStack.toString());
					switch(((PdfOperator)next).id) {
						case 7:
						    drawText(g, page, startPos, length);
						    break;
					}
					argStack.clear();
				}
				else {
					argStack.add(next);
				}
			}
		}
	}
	
	private void drawText(Graphics2D g, Page page, long startPos, long length) {
		int x = 0;
		int y = 0;
		Font font = new Font("null", 0, 0);
		String text = "";
		Color color = Color.BLACK;
		ArrayList<Object> args = new ArrayList();
		while(fScanner.getPosition() < startPos + length) {
			pScanner.skipWhiteSpace();
			Object next = pScanner.scanNext();
			if(next instanceof PdfOperator) {
				System.out.println(((PdfOperator) next) + ", " + args.toString());
				switch(((PdfOperator)next).id) {
					case 55:
						x = ((Long) args.get(0)).intValue();
						y = ((Long) args.get(1)).intValue();
						break;
					case 57:
						font = page.fonts.get(args.get(0)).getFont(Font.PLAIN, ((Long) args.get(1)).intValue());
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
		g.setColor(color);
		g.setFont(font);
		g.drawString(text, page.x, page.y + page.HEIGHT);
	}
}
