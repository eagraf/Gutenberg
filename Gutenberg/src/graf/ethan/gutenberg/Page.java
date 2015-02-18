package graf.ethan.gutenberg;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

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
	
	public HashMap<String, Object> object;
	public HashMap<String, Object> resources;
	public HashMap<String, Object> fontDictionary;
	public HashMap<String, Object> xObjectReferences;
	public HashMap<String, PdfXObject> xObjects;
	
	public PdfObjectReference contents;	
	
	public GraphicsState state;
	
	public Stack<GraphicsState> stateStack;
	
	public HashMap<String, PdfFont> fonts;
	
	public Page(GutenbergScanner scanner, HashMap<String, Object> pageObject, int x, int y) {
		this.scanner = scanner;
		this.x = x;
		this.y = y;
		
		this.object = pageObject;
		
		ArrayList<Integer> rect = getMediaBox(object);
		WIDTH = (int) (rect.get(2) - rect.get(0));
		HEIGHT = (int) (rect.get(3) - rect.get(1));
		
		this.state = new GraphicsState(scanner.gutenbergDrawer, this);
		this.stateStack = new Stack<>();
		
		Point2D p1 = Transform.user_device(0, 0, state);
		Point2D p2 = Transform.user_device(WIDTH, HEIGHT, state);
	
		dWidth = (int) (p2.getX() - p1.getX());
		dHeight = (int) (p1.getY() - p2.getY());
		state.setClip(x, y, WIDTH, HEIGHT);
		
		contents = (PdfObjectReference) pageObject.get("Contents");
		
		getResources();
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
	
	@SuppressWarnings("unchecked")
	public void getResources() {
		resources = (HashMap<String, Object>) object.get("Resources");
		System.out.println("Page Resources: " + resources);
		
		if(resources.containsKey("Font")) {
			fontDictionary = (HashMap<String, Object>) resources.get("Font");
			this.fonts = getFonts();
		}
		
		if(resources.containsKey("XObject")) {
			xObjectReferences = (HashMap<String, Object>) resources.get("XObject");
			getXObjects();
			System.out.println("XObject References: " + xObjectReferences);
			System.out.println("XObjects: " + xObjects);
		}
	}
	
	public void getXObjects() {
		xObjects = new HashMap<String, PdfXObject>();
		Iterator<Entry<String, Object>> it = xObjectReferences.entrySet().iterator();
	    while (it.hasNext()) {
	    	Entry<String, Object> pairs = it.next();
	    	xObjects.put((String) pairs.getKey(), scanner.xObjectScanner.scanObject((PdfObjectReference) pairs.getValue()));
	    }
	}
	
	/*
	 * Retrieves all of the fonts used in the file.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap<String, PdfFont> getFonts() {
		HashMap<String, PdfFont> res = new HashMap<String, PdfFont>();;
		Iterator<Entry<String, Object>> it = fontDictionary.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        Object font = pairs.getValue();
	        if(font.getClass() == HashMap.class) {
		        PdfFont newFont;
		        File fontFile;
		        switch((String)((HashMap<String, Object>) font).get("BaseFont")) {
		        	case "Times-Roman":
		        		fontFile = new File(GutenbergCore.class.getResource("resources/fonts/times.ttf").getFile());
		        		newFont = new PdfFont("Times-Roman", Font.TRUETYPE_FONT, fontFile);
		        		res.put((String) pairs.getKey(), newFont);
		        }
	        }
	        else if(font.getClass() == PdfObjectReference.class) {
	        	PdfFont newFont;
		        File fontFile;
	        	HashMap<String, Object> fontDictionary = (HashMap<String, Object>) scanner.crossScanner.getObject((PdfObjectReference) font);
	        	System.out.println("Font: " + fontDictionary);
	        	switch((String)((HashMap<String, Object>) fontDictionary).get("BaseFont")) {
		        	case "Times-Roman":
		        		fontFile = new File(GutenbergCore.class.getResource("resources/fonts/times.ttf").getFile());
		        		newFont = new PdfFont("Times-Roman", Font.TRUETYPE_FONT, fontFile);
		        		res.put((String) pairs.getKey(), newFont);
	        	}
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    } 
	    System.out.println("Font Dictionary: " + res);
	    return res;
	}
	
	public void pushStack() {
		stateStack.push(state);
		state = new GraphicsState(scanner.gutenbergDrawer, this);
	}
	
	public void popStack() {
		state = stateStack.pop();
	}
}