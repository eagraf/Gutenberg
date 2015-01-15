package graf.ethan.gutenberg;

import java.util.ArrayList;

public class CrossReferenceScanner {
	private PdfScanner pdfScanner;
	private ArrayList<CrossReferenceSection> xrefs;
	
	
	public CrossReferenceScanner(PdfScanner p, ArrayList<CrossReferenceSection> xrefs) {
		this.pdfScanner = p;
		this.xrefs = xrefs;
	}
	
	public Object getObject(PdfObjectReference reference) {
		int index = reference.objectNumber;
		for(int i = 0; i < xrefs.size(); i ++) {
			if(index < xrefs.get(i).startNum + xrefs.get(i).length) {
				pdfScanner.scanner.setPosition(xrefs.get(i).startPos);
				pdfScanner.skipWhiteSpace();
				pdfScanner.scanner.setPosition(pdfScanner.scanner.getPosition() + (20 * (index - xrefs.get(i).startNum)));
				long pos = (long) pdfScanner.scanLong();
				pdfScanner.scanner.setPosition(pos);
				return pdfScanner.scanObject().object;
			}
		}
		return "NO SUCH REFERENCE";
	}
}
