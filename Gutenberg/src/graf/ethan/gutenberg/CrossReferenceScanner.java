package graf.ethan.gutenberg;

import java.util.ArrayList;

/*
 * CrossReferenceScanner retrieves objects from the cross reference table.
 */
public class CrossReferenceScanner {
	private PdfScanner pdfScanner;
	private ArrayList<CrossReferenceSection> xrefs;
	
	private static final int ENTRYSIZE = 20;
	
	
	public CrossReferenceScanner(PdfScanner p, ArrayList<CrossReferenceSection> xrefs) {
		this.pdfScanner = p;
		this.xrefs = xrefs;
		System.out.println(xrefs.get(0).startPos);
	}
	
	/*
	 * Returns the object that is referred to by the reference as an object, 
	 * which can then be cast to the appropriate type.
	 */
	public Object getObject(PdfObjectReference reference) {
		int index = reference.objectNumber;
		//Xrefs are split into multiple segments for each incremental update to the PDF file.
		//This loop finds the correct segment.
		for(int i = 0; i < xrefs.size(); i ++) {
			if(index < xrefs.get(i).startNum + xrefs.get(i).length) {
				//Sets scanner position to beginning of segment.
				pdfScanner.scanner.setPosition(xrefs.get(i).startPos);

				//Xref entries are always 20 characters long. Sets position to the correct entry based on this.
				pdfScanner.skipWhiteSpace();
				pdfScanner.scanner.setPosition(pdfScanner.scanner.getPosition() + (ENTRYSIZE * (index - xrefs.get(i).startNum)));
				
				//Gets the position of the entry's object in the file.
				long pos = (long) pdfScanner.scanLong();
				pdfScanner.scanner.setPosition(pos);
				System.out.println(pos);
				return pdfScanner.scanObject().object;
			}
		}
		//If this reference doesn't exist.
		return "NO REFERENCE";
	}
	
	public Long getObjectPosition(PdfObjectReference reference) {
		int index = reference.objectNumber;
		//Xrefs are split into multiple segments for each incremental update to the PDF file.
		//This loop finds the correct segment.
		for(int i = 0; i < xrefs.size(); i ++) {
			if(index < xrefs.get(i).startNum + xrefs.get(i).length) {
				//Sets scanner position to beginning of segment.
				pdfScanner.scanner.setPosition(xrefs.get(i).startPos);

				//Xref entries are always 20 characters long. Sets position to the correct entry based on this.
				pdfScanner.skipWhiteSpace();
				pdfScanner.scanner.setPosition(pdfScanner.scanner.getPosition() + (ENTRYSIZE * (index - xrefs.get(i).startNum)));
				
				//Gets the position of the entry's object in the file.
				long pos = (long) pdfScanner.scanLong();
				
				return pos;
			}
		}
		//If this reference doesn't exist.
		return null;
	}
}
