package graf.ethan.gutenberg;

import java.awt.Font;
import java.awt.geom.Point2D;
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
		
		ArrayList<Integer> rect = getMediaBox(pageObject);
		WIDTH = (int) (rect.get(2) - rect.get(0));
		HEIGHT = (int) (rect.get(3) - rect.get(1));
		
		this.state = new GraphicsState(scanner.gutenbergDrawer, this);
		
		Point2D p1 = Transform.user_device(0, 0, state);
		Point2D p2 = Transform.user_device(WIDTH, HEIGHT, state);
	
		dWidth = (int) (p2.getX() - p1.getX());
		dHeight = (int) (p1.getY() - p2.getY());
		
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
	public ArrayList<Integer> getMediaBox(HashMap<String, Object> node) {
		ArrayList<Integer> rect;
		if(node.containsKey("MediaBox")) {
			rect = (ArrayList<Integer>) node.get("MediaBox");
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
