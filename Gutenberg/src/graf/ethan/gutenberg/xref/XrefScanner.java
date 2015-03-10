package graf.ethan.gutenberg.xref;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.scanner.PdfScanner;

import java.util.ArrayList;

/*
 * CrossReferenceScanner retrieves objects from the cross reference table.
 */
public class XrefScanner extends Xref{
	private PdfScanner pdfScanner;
	
	private static final int ENTRYSIZE = 20;
	
	public XrefScanner(GutenbergScanner g, ArrayList<XrefSection> xrefs) {
		this.pdfScanner = g.pdfScanner;
		this.xrefSections = xrefs;
	}
	
	/*
	 * Returns the object that is referred to by the reference as an object, 
	 * which can then be cast to the appropriate type.
	 */
	public Object getObject(PdfObjectReference reference) {
		int index = reference.objectNumber;
		//Xrefs are split into multiple segments for each incremental update to the PDF file.
		//This loop finds the correct segment.
		for(int i = 0; i < xrefSections.size(); i ++) {
			if(index < xrefSections.get(i).startNum + xrefSections.get(i).length) {
				//Sets scanner position to beginning of segment.
				pdfScanner.scanner.setPosition(xrefSections.get(i).startPos);

				//Xref entries are always 20 characters long. Sets position to the correct entry based on this.
				pdfScanner.skipWhiteSpace();
				pdfScanner.scanner.setPosition(pdfScanner.scanner.getPosition() + (ENTRYSIZE * (index - xrefSections.get(i).startNum)));
				
				//Gets the position of the entry's object in the file.
				long pos = (long) pdfScanner.scanLong();
				pdfScanner.scanner.setPosition(pos);
				System.out.println("Reference: " + reference + " XREF Scanner Position:" + pos);
				return pdfScanner.scanObject().object;
			}
		}
		//If this reference doesn't exist.
		return "NO REFERENCE";
	}
	
	public long getObjectPosition(PdfObjectReference reference) {
		int index = reference.objectNumber;
		//Xrefs are split into multiple segments for each incremental update to the PDF file.
		//This loop finds the correct segment.
		for(int i = 0; i < xrefSections.size(); i ++) {
			if(index < xrefSections.get(i).startNum + xrefSections.get(i).length) {
				//Sets scanner position to beginning of segment.
				pdfScanner.scanner.setPosition(xrefSections.get(i).startPos);

				//Xref entries are always 20 characters long. Sets position to the correct entry based on this.
				pdfScanner.skipWhiteSpace();
				pdfScanner.scanner.setPosition(pdfScanner.scanner.getPosition() + (ENTRYSIZE * (index - xrefSections.get(i).startNum)));
				
				//Gets the position of the entry's object in the file.
				long pos = (long) pdfScanner.scanLong();
				
				return pos;
			}
		}
		//If this reference doesn't exist.
		return 0;
	}
}
