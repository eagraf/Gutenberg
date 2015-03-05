package graf.ethan.gutenberg.scanner;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.pdf.PdfOperation;
import graf.ethan.gutenberg.pdf.PdfOperator;

import java.util.ArrayList;

/*
 * Scans normal content stream, by extending the filtered scanner.
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
			//If next is an operator, return the operator along with the arguments.
			else if(next.getClass() == PdfOperator.class) {
				PdfOperation res = new PdfOperation((PdfOperator) next, args);
				args = new ArrayList<Object>();
				return res;
			}
			//Add next to the argument stack.
			args.add(next);
		}
		return null;
	}
	
	//Test if the scanner is done with the stream.
	public boolean finished() {
		return filter.finished();
	}
}
