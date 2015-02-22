package graf.ethan.gutenberg.xref;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.filter.Filter;
import graf.ethan.gutenberg.filter.FilterDCT;
import graf.ethan.gutenberg.filter.FilterFlate;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfObjectReference;

import java.util.ArrayList;

public class XrefStreamScanner {
	
	private static final int ENTRYSIZE = 20;
	
	private GutenbergScanner scanner;
	
	public long startPos;
	private long length;
	
	public PdfDictionary streamDictionary;
	
	private ArrayList<XrefSection> xrefs;
	public Filter filter;
	
	//The length of each field in a cross-reference entry.
	private int field1;
	private int field2;
	private int field3;
	
	public XrefStreamScanner(GutenbergScanner scanner) {
		this.scanner = scanner;
	}
	
	public void setStream(PdfObjectReference reference) {
		scanner.fileScanner.setPosition(scanner.crossScanner.getObjectPosition(reference));
		
		//Scan in the stream dictionary
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		PdfDictionary streamDictionary;
		
		if(scanner.pdfScanner.scanKeyword() == 2) {
			streamDictionary = (PdfDictionary) scanner.pdfScanner.scanNext();
			System.out.println("XRef Stream Dictionary: " + streamDictionary);
			
			//Begin the scanning process
			scanner.pdfScanner.scanKeyword();
			scanner.pdfScanner.skipWhiteSpace();
			startPos = scanner.fileScanner.getPosition();
			length = ((Number) streamDictionary.get("Length")).longValue();
			
			//Get the array specifying the different subsections within the table.
			if(streamDictionary.has("Index")) {
				@SuppressWarnings("unchecked")
				ArrayList<Integer> index = (ArrayList<Integer>) streamDictionary.get("Index");
				for(int i = 0; i < index.size(); i += 2) {
					xrefs.add(new XrefSection(index.get(i), index.get(i + 1)));
				}
			}
			
			//Get the array of integers that specifies the length of each field in  the stream.
			if(streamDictionary.has("W")) {
				@SuppressWarnings("unchecked")
				ArrayList<Integer> w = (ArrayList<Integer>) streamDictionary.get("W");
				this.field1 = w.get(0);
				this.field2 = w.get(1);
				this.field3 = w.get(2);
			}
			
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
		}
		else {
			streamDictionary = null;
		}
	}
}
