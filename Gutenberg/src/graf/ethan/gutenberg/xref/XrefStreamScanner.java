package graf.ethan.gutenberg.xref;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.filter.Filter;
import graf.ethan.gutenberg.filter.FilterDCT;
import graf.ethan.gutenberg.filter.FilterFlate;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.scanner.ObjectStreamScanner;

import java.util.ArrayList;

public class XrefStreamScanner extends Xref{
	
	private GutenbergScanner scanner;
	private ObjectStreamScanner objectScanner;
	
	public long startPos;
	private long length;
	
	public PdfDictionary streamDictionary;
	
	public Filter filter;
	
	//The length of each field in a cross-reference entry.
	private int field1 = 1;
	private int field2 = 1;
	private int field3 = 1;
	private int entrySize;
	
	public XrefStreamScanner(GutenbergScanner scanner) {
		this.scanner = scanner;
		this.objectScanner = new ObjectStreamScanner(scanner);
		xrefs = new ArrayList<>();
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
			else {
				xrefs.add(new XrefSection(0, ((Number) streamDictionary.get("Size")).intValue()));
			}
			
			//Get the array of integers that specifies the length of each field in  the stream.
			if(streamDictionary.has("W")) {
				@SuppressWarnings("unchecked")
				ArrayList<Integer> w = (ArrayList<Integer>) streamDictionary.get("W");
				this.field1 = w.get(0);
				this.field2 = w.get(1);
				this.field3 = w.get(2); 
				this.entrySize = field1 + field2 + field3;
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
	
	public Object getObject(PdfObjectReference reference) {
		
		int index = reference.objectNumber;
		int skip = 0;
		//Xrefs are split into multiple segments for each incremental update to the PDF file.
		//This loop finds the correct segment.
		for(int i = 0; i < xrefs.size(); i ++) {
			if(index < xrefs.get(i).startNum + xrefs.get(i).length) {	
				filter.skip((skip * entrySize) + ((index - xrefs.get(i).startNum) * entrySize));
				switch(filter.read()) {
					case 0:
						//Do nothing ... Empty entry.
						return null;
					case 1:
						long loc = 0;
						for(int j = 0; j < field2; j ++) {
							int next = filter.read();
							next = next << (8 * (field2 - j - 1));
							loc += next;
						}
						@SuppressWarnings("unused")
						long gen = 0;
						for(int k = 0; k < field3; k ++) {
							int next = filter.read();
							next = next << (8 * (field3 - k - 1));
							gen += next;
						}
						scanner.pdfScanner.scanner.setPosition(loc);
						System.out.println("Reference: " + reference + " XREF Scanner Position: " + loc);
						filter.reset();
						return scanner.pdfScanner.scanNext();
					case 2:
						long streamNum = 0;
						for(int j = 0; j < field2; j ++) {
							int next = filter.read();
							next = next << (8 * (field2 - j - 1));
							streamNum += next;
						}
						long objNum = 0;
						for(int k = 0; k < field3; k ++) {
							int next = filter.read();
							next = next << (8 * (field3 - k - 1));
							objNum += next;
						}
						System.out.println("Reference: " + reference + " Object Stream: " + new PdfObjectReference((int) streamNum, 0));
						filter.reset();
						objectScanner.setStream(getObjectPosition(new PdfObjectReference((int) streamNum, 0)));
						return objectScanner.getObject((int) objNum);
				}
			}
			else {
				skip += xrefs.get(i).length;
				
			}
		}
		//If this reference doesn't exist.
		return "NO REFERENCE";
	}

	@Override
	public long getObjectPosition(PdfObjectReference reference) {
		int index = reference.objectNumber;
		int skip = 0;
		//Xrefs are split into multiple segments for each incremental update to the PDF file.
		//This loop finds the correct segment.
		for(int i = 0; i < xrefs.size(); i ++) {			
			if(index < xrefs.get(i).startNum + xrefs.get(i).length) {	
				filter.skip((skip * entrySize) + ((index - xrefs.get(i).startNum) * entrySize));
				switch(filter.read()) {
					case 0:
						//Do nothing ... Empty entry.
						return -1;
					case 1:
						long loc = 0;
						for(int j = 0; j < field2; j ++) {
							int next = filter.read();
							next = next << (8 * (field2 - j - 1));
							loc += next;
						}
						long gen = 0;
						for(int k = 0; k < field3; k ++) {
							int next = filter.read();
							next = next << (8 * (field3 - k - 1));
							gen += next;
						}
						scanner.pdfScanner.scanner.setPosition(loc);
						System.out.println("Reference: " + reference + " XREF Scanner Position: " + loc);
						filter.reset();
						return loc;
					case 2:
						//Objects in object stream do not have specified position.
						return -1;
				}
			}
			else {
				skip += xrefs.get(i).length;
				
			}
		}
		//If this reference doesn't exist.
		return -1;
	}
}
