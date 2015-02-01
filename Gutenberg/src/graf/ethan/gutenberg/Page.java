package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/*
 * Represents a single page in the PDF. 
 * Still needs to be expanded.
 */
public class Page {
	//WIDTH and HEIGHT in user space
	public final int WIDTH;
	public final int HEIGHT;
	
	//Width and Height in device space
	public int dWidth;
	public int dHeight;
	
	//Coordinates in device space
	public int x;
	public int y;
	
	public GutenbergScanner scanner;
	public HashMap<String, Object> resources;
	public HashMap<String, Object> fontDictionary;
	public PdfObjectReference contents;	
	
	public GraphicsState state;
	
	public HashMap<String, PdfFont> fonts;
	
	@SuppressWarnings("unchecked")
	public Page(GutenbergScanner scanner, HashMap<String, Object> pageObject, int x, int y) {
		this.scanner = scanner;
		this.x = x;
		this.y = y;
		
		ArrayList<Long> rect = getMediaBox(pageObject);
		WIDTH = rect.get(2).intValue() - rect.get(0).intValue();
		HEIGHT = rect.get(3).intValue() - rect.get(1).intValue();
		
		this.state = new GraphicsState(scanner.gutenbergDrawer, this);
		
		double ul[][] = {{0d}, {0d}, {1d}};
		double lr[][] = {{WIDTH}, {HEIGHT}, {1d}};
		
		Matrix ulc = new Matrix(ul);
		Matrix lrc = new Matrix(lr);
		
		Matrix m1, m2;
		
		ul = ulc.multiply(state.ctm).getGraph();
		lr = lrc.multiply(state.ctm).getGraph();
		
		dWidth = (int) (lr[0][0] - ul[0][0]);
		dHeight = (int) (lr[1][0] - ul[1][0]);
		
		contents = (PdfObjectReference) pageObject.get("Contents");
		resources = (HashMap<String, Object>) pageObject.get("Resources");
		System.out.println(resources);
		fontDictionary = (HashMap<String, Object>) resources.get("Font");
		
		this.fonts = scanFonts();
	}
	
	/*
	 * Gets the dimensions of the page.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Long> getMediaBox(HashMap<String, Object> node) {
		ArrayList<Long> rect;
		if(node.containsKey("MediaBox")) {
			rect = (ArrayList<Long>) node.get("MediaBox");
		}
		else {
			rect = getMediaBox((HashMap<String, Object>) scanner.crossScanner.getObject((PdfObjectReference) node.get("Parent")));
		}
		return rect;
	}
	
	/*
	 * Retrieves all of the fonts used in the file.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap<String, PdfFont> scanFonts() {
		HashMap<String, PdfFont> res = new HashMap<String, PdfFont>();;
		if(fontDictionary == null) {
			return null;
		}
		Iterator<Entry<String, Object>> it = fontDictionary.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        HashMap<String, Object> font = (HashMap<String, Object>) pairs.getValue();
	        PdfFont newFont;
	        File fontFile;
	        switch((String)font.get("BaseFont")) {
	        	case "Times-Roman":
	        		fontFile = new File(GutenbergCore.class.getResource("resources/fonts/times.ttf").getFile());
	        		newFont = new PdfFont("Times-Roman", Font.TRUETYPE_FONT, fontFile);
	        		res.put((String) pairs.getKey(), newFont);
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    } 
	    System.out.println(res);
	    return res;
	}
}
