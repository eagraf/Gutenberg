package graf.ethan.gutenberg.scanner;

import java.io.IOException;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.filter.Filter;
import graf.ethan.gutenberg.filter.FilterDCT;
import graf.ethan.gutenberg.filter.FilterFlate;
import graf.ethan.gutenberg.pdf.PdfDictionary;

public class ObjectStreamScanner extends FilteredScanner {
	
	private GutenbergScanner scanner;
	
	public long startPos;
	private long length;
	
	public PdfDictionary streamDictionary;
	
	private int num;
	private int first;
	
	public ObjectStreamScanner(GutenbergScanner scanner) {
		super(scanner);
		this.scanner = scanner;
	}

	public void setStream(long position) {
		scanner.fileScanner.setPosition(position);
		
		//Scan in the stream dictionary
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		PdfDictionary streamDictionary;
		
		if(scanner.pdfScanner.scanKeyword() == 2) {
			streamDictionary = (PdfDictionary) scanner.pdfScanner.scanNext();
			System.out.println("Object Stream Dictionary: " + streamDictionary);
			
			//Begin the scanning process
			scanner.pdfScanner.scanKeyword();
			scanner.pdfScanner.skipWhiteSpace();
			startPos = scanner.fileScanner.getPosition();
			length = ((Number) streamDictionary.get("Length")).longValue();
			
			PdfDictionary params = null;
			if(streamDictionary.has("DecodeParms")) {
				params = (PdfDictionary) streamDictionary.get("DecodeParms");
			}
			
			if(streamDictionary.has("Filter")) {
				String filterName = (String) streamDictionary.get("Filter");
				switch(filterName) {
					case "FlateDecode":
						filter = new FilterFlate(startPos, length, params, scanner.fileScanner.file);
						break;
					case "DCTDecode":
						filter = new FilterDCT(startPos, length, scanner.fileScanner.file);
						break;
				}
			}
			else {
				filter = new Filter(startPos, length, scanner.fileScanner.file);
			}
			
			if(streamDictionary.has("N")) {
				this.num = (int) streamDictionary.get("N");
			}
			
			if(streamDictionary.has("First")) {
				this.first = (int) streamDictionary.get("First");
			}
		}
		else {
			streamDictionary = null;
		}
	}
	
	public Object getObject(int index) {
		filter.reset();
		
		long pos = getPosition(index);
		if(pos == 0) {
			return null;
		}
		filter.reset();
		filter.skip(pos + first);
		
		return scanNext();
	}

	public long getPosition(int index) {
		for(int i = 0; i < index; i ++) {
			scanNext();
			scanNext();
		}
		scanNext();
		return ((Number) scanNext()).longValue();
	}
}
