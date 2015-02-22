package graf.ethan.gutenberg.pdf;

public class PdfObjectReference {
	public int objectNumber;
	public int generationNumber;
	
	public PdfObjectReference(int objectNumber, int generationNumber) {
		this.objectNumber = objectNumber;
		this.generationNumber = generationNumber;
	}
	
	@Override
	public String toString() {
		return objectNumber + " " + generationNumber + " " + "R"; 
	}
}
