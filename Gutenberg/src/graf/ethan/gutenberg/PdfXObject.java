package graf.ethan.gutenberg;

import java.util.HashMap;

public class PdfXObject {
	public Object object;
	
	public HashMap<String, Object> dictionary;
	
	public PdfXObject(HashMap<String, Object> dictionary, Object object) {
		this.dictionary = dictionary;
		this.object = object;
	}
}
