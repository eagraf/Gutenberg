package graf.ethan.gutenberg;

import java.util.HashMap;

public class PdfDictionary {
	
	private HashMap<String, Object> dict;
	private CrossReferenceScanner crossScanner;
	
	public PdfDictionary(HashMap<String, Object> dict, GutenbergScanner scanner) {
		this.dict = dict;
		this.crossScanner = scanner.crossScanner;
	}
	
	public Object get(String key) {
		if(dict.containsKey(key)) {
			Object value = dict.get(key);
			if(value.getClass() == PdfObjectReference.class) {
				return crossScanner.getObject((PdfObjectReference) value);
			}
			else {
				return value;
			}
		}
		return null;
	}
	
	public PdfObjectReference getReference(String key) {
		if(dict.containsKey(key)) {
			if(dict.get(key).getClass() == PdfObjectReference.class) {
				return (PdfObjectReference) dict.get(key);
			}
		}
		return null;
	}
	
	public boolean has(String key) {
		if(dict.containsKey(key)) {
			return true;
		}
		return false;
	}
	
	public HashMap<String, Object> getDict() {
		return dict;
	}
	
	@Override
	public String toString() {
		return dict.toString();
	}
}
