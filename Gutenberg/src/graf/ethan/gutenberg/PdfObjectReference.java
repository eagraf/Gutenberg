package graf.ethan.gutenberg;

public class PdfObjectReference {
	int objectNumber;
	int generationNumber;
	
	public PdfObjectReference(int objectNumber, int generationNumber) {
		this.objectNumber = objectNumber;
		this.generationNumber = generationNumber;
	}
	
	@Override
	public String toString() {
		return objectNumber + " " + generationNumber + " " + "R"; 
	}
}
