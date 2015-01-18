package graf.ethan.gutenberg;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * Represents a single page in the PDF. 
 * Still needs to be expanded.
 */
public class Page {
	public final int WIDTH;
	public final int HEIGHT;
	
	private GutenbergScanner scanner;
	private HashMap<String, Object> resources;
	private HashMap<String, Object> fontDictionary;
	public HashMap<String, PdfFont> fonts;
	
	public Page(GutenbergScanner scanner, HashMap<String, Object> pageObject) {
		this.scanner = scanner;
		
		ArrayList<Integer> rect = getMediaBox(pageObject);
		WIDTH= rect.get(2) - rect.get(0);
		HEIGHT= rect.get(3) - rect.get(1);
		
		resources = (HashMap<String, Object>) pageObject.get("Resources");
		System.out.println(resources);
		fontDictionary = (HashMap<String, Object>) resources.get("Font");
		
		this.fonts = scanFonts();
	}
	
	public ArrayList<Integer> getMediaBox(HashMap<String, Object> node) {
		ArrayList<Integer> rect;
		if(node.containsKey("MediaBox")) {
			rect = (ArrayList) node.get("MediaBox");
		}
		else {
			rect = getMediaBox((HashMap<String, Object>) scanner.crossScanner.getObject((PdfObjectReference) node.get("Parent")));
		}
		return rect;
	}
	
	public HashMap<String, PdfFont> scanFonts() {
		HashMap<String, PdfFont> res = new HashMap();;
		Iterator it = fontDictionary.entrySet().iterator();
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
