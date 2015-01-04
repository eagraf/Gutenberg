package graf.ethan.gutenberg;

public class PdfObject {
	int objectNumber;
	int generationNumber;
	Object object;
	
	public PdfObject(int objnum, int gennum, Object object) {
		this.objectNumber = objnum;
		this.generationNumber = gennum;
		this.object = object;
	}
}
