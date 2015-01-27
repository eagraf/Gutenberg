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
			
			ArrayList<Object> argStack = new ArrayList<Object>();
			while(fScanner.getPosition() < stream.endPos) {
				pScanner.skipWhiteSpace();
				Object next = pScanner.scanNext();
				if(next instanceof PdfOperator) {
					System.out.println(((PdfOperator) next) + ", " + argStack.toString());
					switch(((PdfOperator)next).id) {
						case 7:
						    scanText(g, page, stream);
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
					case 55:
						x = ((Long) args.get(0)).intValue();
						y = ((Long) args.get(1)).intValue();
						break;
					case 57:
						font = (String) args.get(0);
						size = ((Long) args.get(1)).intValue() * 4 / 3;
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
