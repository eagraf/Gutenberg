package graf.ethan.gutenberg.font;

import java.nio.ByteBuffer;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.filter.DCTDecode;
import graf.ethan.gutenberg.filter.Filterless;
import graf.ethan.gutenberg.filter.FlateDecode;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.scanner.FileScanner;
import graf.ethan.gutenberg.scanner.FilteredScanner;

/*
 * FontStreamScanner reads embedded font programs into a buffer, so that it can be read by the FontScanner classes.
 */
public class FontStreamScanner extends FilteredScanner {
	
	public long length1;
	
	public FontStreamScanner(GutenbergScanner scanner) {
		super(scanner);
	}
	
	public ByteBuffer getData(int type) {
		ByteBuffer data;
		switch(type) {
			case -1:
				//TrueType Font
				//Allocates a buffer the length of the encoded data
				data = ByteBuffer.allocate((int) length1);
				System.out.println(length1);
				//Copy the data into memory.
				for(int i = 0; i < length1; i ++) {
					byte dat = (byte) nextChar();
					data.put(dat);
				}
				return data;
		}
		return null;
	}
	
	public Font scanTrueType(PdfObjectReference ref) {
		setStream(ref);
		//Get the font program within the PDF file.
		ByteBuffer data = getData(-1);
		TrueTypeScanner ttScanner = new TrueTypeScanner(data);
		return new Font();
	}
	
	@Override
	public void setStream(PdfObjectReference reference) {
		System.out.println(scanner.crossScanner);
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
			length = ((Number) streamDictionary.get("Length")).longValue();
		}
		else {
			streamDictionary = null;
		}
		
		PdfDictionary params = null;
		if(streamDictionary.has("DecodeParms")) {
			params = (PdfDictionary) streamDictionary.get("DecodeParms");
		}
		
		//Begin the scanning process
		scanner.pdfScanner.scanKeyword();
		scanner.pdfScanner.skipWhiteSpace();
		startPos = scanner.fileScanner.getPosition();
		
		//This is the overrided section. Gets the length of the decoded data.
		if(streamDictionary.has("Length1")) {
			this.length1 = ((Number) streamDictionary.get("Length1")).longValue();
			System.out.println("Length: " + length); 
		}
		
		if(streamDictionary.has("Filter")) {
			String filterName = (String) streamDictionary.get("Filter");
			switch(filterName) {
				case "FlateDecode":
					filter = new FlateDecode(startPos, length, params, scanner.fileScanner.file);
					break;
				case "DCTDecode":
					filter = new DCTDecode(startPos, length, scanner.fileScanner.file);
					break;
			}
		}
		else {
			filter = new Filterless(startPos, length, scanner.fileScanner.file);
		}
	}
	
	

}
