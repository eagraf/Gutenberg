package graf.ethan.gutenberg;

import java.util.ArrayList;

public class XrefStreamScanner {
	
	private static final int ENTRYSIZE = 20;
	
	private GutenbergScanner scanner;
	
	public long startPos;
	private long length;
	
	public PdfDictionary streamDictionary;
	
	private ArrayList<XrefSection> xrefs;
	public Filter filter;
	
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
			System.out.println("Stream Dictionary: " + streamDictionary);
			
			//Begin the scanning process
			scanner.pdfScanner.scanKeyword();
			scanner.pdfScanner.skipWhiteSpace();
			startPos = scanner.fileScanner.getPosition();
			length = ((Number) streamDictionary.get("Length")).longValue();
			
			if(streamDictionary.has("Index")) {
				@SuppressWarnings("unchecked")
				ArrayList<Integer> index = (ArrayList<Integer>) streamDictionary.get("Index");
				for(int i = 0; i < index.size(); i += 2) {
					xrefs.add(new XrefSection(index.get(i), index.get(i + 1)));
				}
			}
			
			PdfDictionary params = null;
			if(streamDictionary.has("DecodeParms")) {
				params = (PdfDictionary) streamDictionary.get("DecodeParms");
			}
			
			if(streamDictionary.has("Filter")) {
				String filterName = (String) streamDictionary.get("Filter");
				switch(filterName) {
					case "FlateDecode":
						filter = new FilterFlate(startPos, length, scanner.fileScanner.file);
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
