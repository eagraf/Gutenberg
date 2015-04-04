package graf.ethan.gutenberg.pdf;

import graf.ethan.gutenberg.core.GutenbergCore;
import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.misc.Transform;
import graf.ethan.gutenberg.resources.fonts.DumDum;

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
	
	public PdfDictionary object;
	public PdfDictionary resources;
	public PdfDictionary fontDictionary;
	public PdfDictionary xObjectReferences;
	public HashMap<String, PdfXObject> xObjects;
	
	public ArrayList<PdfObjectReference> contents;	
	
	public GraphicsState state;
	
	public Stack<GraphicsState> stateStack;
	
	public HashMap<String, PdfFont> fonts;
	
	@SuppressWarnings("unchecked")
	public Page(GutenbergScanner scanner, PdfDictionary pageObject, int x, int y) {
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
		state.setClip(x, y, dWidth, dHeight);
		
		this.contents = new ArrayList<PdfObjectReference>();
		Object temp = pageObject.getDict().get("Contents");
		if(temp.getClass() == PdfObjectReference.class) {
			this.contents.add((PdfObjectReference) temp);
		}
		else if(temp.getClass() == ArrayList.class) {
			this.contents = (ArrayList<PdfObjectReference>) temp;
		}
		System.out.println(contents.getClass());
		System.out.println("Page: " + object);
		System.out.println("Page Contents: " + contents);
		getResources();
	}
	
	/*
	 * Gets the dimensions of the page.
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getMediaBox(PdfDictionary object) {
		ArrayList<Integer> rect;
		if(object.has("MediaBox")) {
			rect = (ArrayList<Integer>) object.get("MediaBox");
		}
		else {
			rect = getMediaBox((PdfDictionary) object.get("Parent"));
		}
		return rect;
	}
	
	public void getResources() {
		//Get the resources dictionary.
		if(object.has("Resources")) {
			resources = (PdfDictionary) object.get("Resources");
			System.out.println("Page Resources: " + resources);
		}
		
		//Get the fonts used by the page.
		if(resources.has("Font")) {
			fontDictionary = (PdfDictionary) resources.get("Font");
			this.fonts = getFonts();
		}
		
		//Get the external objects used by the page.
		if(resources.has("XObject")) {
			xObjectReferences = (PdfDictionary) resources.get("XObject");
			getXObjects();
			System.out.println("XObject References: " + xObjectReferences);
			System.out.println("XObjects: " + xObjects);
		}
	}
	
	/*
	 * Scan the external objects into the xObjects dictionary.
	 */
	public void getXObjects() {
		xObjects = new HashMap<String, PdfXObject>();
		Iterator<Entry<String, Object>> it = xObjectReferences.getDict().entrySet().iterator();
	    while (it.hasNext()) {
	    	Entry<String, Object> pairs = it.next();
	    	System.out.println(pairs.getValue());
	    	xObjects.put((String) pairs.getKey(), scanner.xObjectScanner.scanObject((PdfObjectReference) pairs.getValue()));
	    }
	}
	
	/*
	 * Retrieves all of the fonts used in the file.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap<String, PdfFont> getFonts() {
		HashMap<String, PdfFont> res = new HashMap<String, PdfFont>();;
		Iterator<Entry<String, Object>> it = fontDictionary.getDict().entrySet().iterator();
	    while (it.hasNext()) {
	    	System.out.println("Font:");
	        Map.Entry pairs = (Map.Entry)it.next();
	        Object font = pairs.getValue();
	        PdfDictionary dict = null;
	        if(font.getClass() == HashMap.class) {
	        	 dict = new PdfDictionary((HashMap<String, Object>)  font, scanner);
	        }
	        else if(font.getClass() == PdfObjectReference.class) {
	        	dict = (PdfDictionary) scanner.crossScanner.getObject((PdfObjectReference) font);
	        }
	        res.put((String) pairs.getKey(), getFont(dict));
	        it.remove(); // avoids a ConcurrentModificationException
	    } 
	    System.out.println("Font Dictionary: " + res);
	    return res;
	}
	
	/*
	 * Push a graphics state onto the graphics state stack.
	 */
	public void pushStack() {
		stateStack.push(state);
		state = new GraphicsState(scanner.gutenbergDrawer, this);
	}
	
	/*
	 * Pop a graphics state from the graphics state stack.
	 */
	public void popStack() {
		state = stateStack.pop();
	}
	
	/*
	 * Get a specific font. Called by the getFonts() method.
	 */
	public PdfFont getFont(PdfDictionary font) {
		PdfFont newFont = null;
        File fontFile;
        //Switch through standard fonts and read the embedded font it it is there.
        switch((String)((PdfDictionary) font).get("BaseFont")) {
        	case "Times-Roman":
        		fontFile = new File(DumDum.class.getResource("times.ttf").getFile());
        		newFont = new PdfFont("Times-Roman", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Times-Bold":
        		fontFile = new File(DumDum.class.getResource("timesbd.ttf").getFile());
        		newFont = new PdfFont("Times-Bold", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Times-Italic":
        		fontFile = new File(DumDum.class.getResource("timesi.ttf").getFile());
        		newFont = new PdfFont("Times-Italic", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Times-BoldItalic":
        		fontFile = new File(DumDum.class.getResource("timesbi.ttf").getFile());
        		newFont = new PdfFont("Times-BoldItalic", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Helvetica":
        		fontFile = new File(DumDum.class.getResource("arial.ttf").getFile());
        		newFont = new PdfFont("Helvetica", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Helvetica-Bold":
        		fontFile = new File(DumDum.class.getResource("arialbd.ttf").getFile());
        		newFont = new PdfFont("Helvetica-Bold", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Helvetica-Oblique":
        		fontFile = new File(DumDum.class.getResource("ariali.ttf").getFile());
        		newFont = new PdfFont("Helvetica-Oblique", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Helvetica-BoldOblique":
        		fontFile = new File(DumDum.class.getResource("arialbi.ttf").getFile());
        		newFont = new PdfFont("Helvetica-BoldOblique", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Courier":
        		fontFile = new File(DumDum.class.getResource("cour.ttf").getFile());
        		newFont = new PdfFont("Courier", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Courier-Bold":
        		fontFile = new File(DumDum.class.getResource("courbd.ttf").getFile());
        		newFont = new PdfFont("Courier-Bold", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Courier-Oblique":
        		fontFile = new File(DumDum.class.getResource("courii.ttf").getFile());
        		newFont = new PdfFont("Times-Roman", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Courier-BoldOblique":
        		fontFile = new File(DumDum.class.getResource("courbi.ttf").getFile());
        		newFont = new PdfFont("Courier-BoldOblique", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "Symbol":
        		fontFile = new File(DumDum.class.getResource("symbol.ttf").getFile());
        		newFont = new PdfFont("Symbol", Font.TRUETYPE_FONT, fontFile);
        		break;
        	case "ZapfDingbats":
        		fontFile = new File(DumDum.class.getResource("zapf-dingbats-bt.ttf").getFile());
        		newFont = new PdfFont("ZapfDingbats", Font.TRUETYPE_FONT, fontFile);
        		break;
        	default:
        		PdfDictionary descriptor;
        		//Scan the embedded font file.
        		if(((PdfDictionary) font).has("FontDescriptor")) {
        			System.out.println("Descriptor");
        			descriptor = (PdfDictionary) font.get("FontDescriptor");
        			if(descriptor.has("FontFile")) {
        				System.out.println("Type 1 Font");
        			}
        			else if(descriptor.has("FontFile2")) {
        				System.out.println("TrueType Font");
        				scanner.fontScanner.scanTrueType((PdfObjectReference) descriptor.getReference("FontFile2"));
        			}
        			else if(descriptor.has("FontFile3")) {
        				System.out.println("Type 1 Font");
        			}
        			else {
        				System.out.println("Default");
            			fontFile = new File(DumDum.class.getResource("times.ttf").getFile());
    	        		newFont = new PdfFont("Times-Roman", Font.TRUETYPE_FONT, fontFile);
        			}
        		}
        		//Revert to a default font file.
        		else {
        			System.out.println("Default");
        			fontFile = new File(DumDum.class.getResource("times.ttf").getFile());
	        		newFont = new PdfFont("Times-Roman", Font.TRUETYPE_FONT, fontFile);
        		}
        		break;
        }
        return newFont;
	}
}