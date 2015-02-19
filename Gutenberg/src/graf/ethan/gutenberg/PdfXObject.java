package graf.ethan.gutenberg;

public class PdfXObject {
	public Object object;
	
	public PdfDictionary dictionary;
	
	public PdfXObject(PdfDictionary dictionary, Object object) {
		this.dictionary = dictionary;
		this.object = object;
	}
}
