package graf.ethan.gutenberg.scanner;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.pdf.PdfOperation;
import graf.ethan.gutenberg.pdf.PdfOperator;

import java.util.ArrayList;

/*
 * Similar to PdfScanner, but does not have random access to file. Reads in data through a filter.
 */
public class StreamScanner extends FilteredScanner {
	
	
	public GutenbergScanner scanner;
	
	public ArrayList<Object> args;
	
	public StreamScanner(GutenbergScanner scanner) {
		super(scanner);
		this.scanner = scanner;
		args = new ArrayList<>();
	}
	
	/*
	 * Scans in PDF objects until an operator is reached, at which point a PDF Operation will be returned.
	 */
	public PdfOperation nextOperation() {
		while(!finished()) {
			Object next = scanNext();
			skipWhiteSpace();
			if(next == null) {
				return null;
			}
			else if(next.getClass() == PdfOperator.class) {
				PdfOperation res = new PdfOperation((PdfOperator) next, args);
				args = new ArrayList<Object>();
				return res;
			}
			args.add(next);
		}
		return null;
	}
	
	public boolean finished() {
		return filter.finished();
	}
}
