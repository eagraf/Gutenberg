package graf.ethan.gutenberg.pdf;

public class PdfObject {
	public int objectNumber;
	public int generationNumber;
	public Object object;
	
	public PdfObject(int objnum, int gennum, Object object) {
		this.objectNumber = objnum;
		this.generationNumber = gennum;
		this.object = object;
	}
	
	@Override
	public String toString() {
		return object.toString();
	}
}
