package graf.ethan.gutenberg.pdf;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.xref.Xref;

import java.util.HashMap;

/*
 * Class that represents a dictionary PDF object. Data is represented as a HashMap.
 */
public class PdfDictionary {
	
	private HashMap<String, Object> dict;
	public Xref crossScanner;
	
	public PdfDictionary(HashMap<String, Object> dict, GutenbergScanner scanner) {
		this.dict = dict;
		this.crossScanner = scanner.crossScanner;
	}
	
	public PdfDictionary(HashMap<String, Object> dict) {
		this.dict = dict;
	}
	
	/*
	 * Retrieve an object from the HashMap. 
	 * If the object is a reference to an indirect object, get the indirect object.
	 */
	public Object get(String key) {
		if(dict.containsKey(key)) {
			Object value = dict.get(key);
			if(value.getClass() == PdfObjectReference.class) {
				System.out.println(crossScanner);
				return crossScanner.getObject((PdfObjectReference) value);
			}
			else {
				return value;
			}
		}
		return null;
	}
	
	/*
	 * Get method that does not automatically scan a reference.
	 */
	public PdfObjectReference getReference(String key) {
		if(dict.containsKey(key)) {
			if(dict.get(key).getClass() == PdfObjectReference.class) {
				return (PdfObjectReference) dict.get(key);
			}
		}
		return null;
	}
		
	/*
	 * Test if a certain entry is in the dictionary.
	 */
	public boolean has(String key) {
		if(dict.containsKey(key)) {
			return true;
		}
		return false;
	}
	
	public HashMap<String, Object> getDict() {
		return dict;
	}
	
	public void setCrossScanner(Xref xref) {
		this.crossScanner = xref;
	}
	
	@Override
	public String toString() {
		return dict.toString();
	}
}
