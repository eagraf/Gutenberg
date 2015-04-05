package graf.ethan.gutenberg.hint;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.filter.Filterless;
import graf.ethan.gutenberg.filter.DCTDecode;
import graf.ethan.gutenberg.filter.FlateDecode;
import graf.ethan.gutenberg.pdf.PdfDictionary;

public class HintScanner {
	
	private GutenbergScanner scanner;
	
	public long startPos;
	private long length;
	
	public PdfDictionary streamDictionary;
	
	public Filterless filter;
	
	public HintTable hintTable;
	
	public int curr;
	public int off = 8;
	
	public HintScanner(GutenbergScanner scanner) {
		this.scanner = scanner;
		this.hintTable = new HintTable();
	}
	
	public void scanOffsetHeader(int num) {
		hintTable.pageOffset1 = (int) nextNum(32);
		hintTable.pageOffset2 = (int) nextNum(32);
		hintTable.pageOffset3 = (int) nextNum(16);
		hintTable.pageOffset4 = (int) nextNum(32);
		hintTable.pageOffset5 = (int) nextNum(16);
		hintTable.pageOffset6 = (int) nextNum(32);
		hintTable.pageOffset7 = (int) nextNum(16);
		hintTable.pageOffset8 = (int) nextNum(32);
		hintTable.pageOffset9 = (int) nextNum(16);
		hintTable.pageOffset10 = (int) nextNum(16);
		hintTable.pageOffset11 = (int) nextNum(16);
		hintTable.pageOffset12 = (int) nextNum(16);
		hintTable.pageOffset13 = (int) nextNum(16);
		
		scanPageOffset(num);
	}
	
	public void scanPageOffset(int num) {
		
		hintTable.offsets = new PageHintTable[num];
		//Scan number of objects ... add to pageOffset1 ... size pageOffset3
		for(int i = 0; i < num; i ++) {
			hintTable.offsets[i] = new PageHintTable();
			hintTable.offsets[i].objNum = (int) (hintTable.pageOffset1 + nextNum(hintTable.pageOffset3));
			System.out.println("Object Number: " + hintTable.offsets[i].objNum);
		}
		//Page length ... add to pageOffset4 ... size pageOffset5
		for(int i = 0; i < num; i ++) {
			hintTable.offsets[i].objLen = (long) (hintTable.pageOffset4 + nextNum(hintTable.pageOffset5));
			System.out.println("Object Length: " + hintTable.offsets[i].objLen);
		}
		//Shared object number ... size pageOffset10
		for(int i = 0; i < num; i ++) {
			hintTable.offsets[i].sharedObjNum = (int) (nextNum(hintTable.pageOffset10));
			hintTable.offsets[i].sharedObjID = new int[hintTable.offsets[i].sharedObjNum];
			hintTable.offsets[i].numerator = new int[hintTable.offsets[i].sharedObjNum];
			System.out.println("Shared Object Number: " + hintTable.offsets[i].sharedObjNum);
			
			//Shared object identifier ... size pageOffset11
			for(int j = 0; j < hintTable.offsets[i].sharedObjNum; j ++) {
				hintTable.offsets[i].sharedObjID[j] = (int) (nextNum(hintTable.pageOffset11));
				System.out.println("Shared Object ID: " + hintTable.offsets[i].sharedObjID[j]);
			}
			//Numerator ... size pageOffset12
			for(int j = 0; j < hintTable.offsets[i].sharedObjNum; j ++) {
				hintTable.offsets[i].numerator[j] = (int) (nextNum(hintTable.pageOffset12));
				System.out.println("Numerator: " + hintTable.offsets[i].numerator[j]);
			}
		}
		//Content stream offset ... add to pageOffset6 ... size to pageOffset6
		for(int i = 0; i < num; i ++) {
			hintTable.offsets[i].streamLen = (long) (hintTable.pageOffset6 + nextNum(hintTable.pageOffset7));
			System.out.println("Stream Offset: " + hintTable.offsets[i].streamLen);
		}
		//Content stream length ... add to pageOffset7 ... size pageOffset9
		for(int i = 0; i < num; i ++) {
			hintTable.offsets[i].streamLen = (int) (hintTable.pageOffset7 + nextNum(hintTable.pageOffset9));
			System.out.println("Stream Length: " + hintTable.offsets[i].streamLen);
		}
	}
	 
	public long nextNum(int bits) {
		long res = 0;
		for(int i = 0; i < bits; i ++) {
			res += nextBit() << (bits - i - 1);
		}
		return res;
	}
	
	public int nextBit() {
		if(off == 8) {
			curr = filter.read();
			off = 0;
		}
		int res = (curr >> (7-off)) & 1;
		off ++;
		return res;
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
			System.out.println("Hint Scanner Stream Dictionary: " + streamDictionary);
			
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
		else {
			streamDictionary = null;
		}
	}
}