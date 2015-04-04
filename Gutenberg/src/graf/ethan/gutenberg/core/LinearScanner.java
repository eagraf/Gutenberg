package graf.ethan.gutenberg.core;

import java.util.ArrayList;

import graf.ethan.gutenberg.hint.HintScanner;
import graf.ethan.gutenberg.pdf.Page;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.scanner.FileScanner;
import graf.ethan.gutenberg.scanner.InitialScanner;
import graf.ethan.gutenberg.scanner.PdfScanner;
import graf.ethan.gutenberg.scanner.StreamScanner;
import graf.ethan.gutenberg.scanner.XObjectScanner;
import graf.ethan.gutenberg.xref.XrefList;

/*
 * The LinearScanner is a special class that scans only the intial stages of Linear PDF files.
 */
public class LinearScanner {
	
	//Scanners
	private GutenbergScanner scanner;
	public FileScanner fileScanner;
	public PdfScanner pdfScanner;
	public StreamScanner streamScanner;
	public XObjectScanner xObjectScanner;
	public HintScanner hintScanner;
	
	public XrefList crossScanner;
	
	public PdfDictionary params;
	
	@SuppressWarnings("unchecked")
	public LinearScanner(GutenbergScanner scanner, PdfDictionary lpDict) {
		//Scanners
		this.scanner = scanner;
		this.fileScanner = scanner.fileScanner;
		this.pdfScanner = scanner.pdfScanner;
		this.streamScanner = scanner.streamScanner;
		this.xObjectScanner = scanner.xObjectScanner;
		this.hintScanner = new HintScanner(scanner); 
		this.crossScanner = new XrefList();
		
		this.params = lpDict;
		
		//Get cross-reference table
		pdfScanner.skipWhiteSpace();
		long xrefPos = fileScanner.getPosition();
		getXref(xrefPos);
		
		//Set up hintscanner.
		hintScanner.setStream(((ArrayList<Number>) params.get("H")).get(0).longValue());
		hintScanner.scanOffsetHeader(((Number) params.get("N")).intValue());
	}
	
	/*
	 *Get the Cross-Reference Table. 
	 */
	public void getXref(long xrefPos) {
		InitialScanner initialScanner = new InitialScanner(scanner);
		initialScanner.scanXrefSection(initialScanner.scanXrefSection(xrefPos));
		scanner.crossScanner = this.crossScanner = initialScanner.getXref();
	}

	public Page getPage(int num) {
		//If it is the first page, read from the beginning section.
		if(num == 0) {
			Page page = new Page(scanner, 
					(PdfDictionary) crossScanner.getObject(new PdfObjectReference((int) params.get("O"), 0)),
					50, 50);
			System.out.println("Page: " + page.contents);
			return page;
		}
		//Otherwise, read from the main section
		if(num < ((Number) params.get("N")).intValue()) {
			int objNum = 1;
			//Reach the appropriate section within the hint table.
			for(int i = 1; i < num; i ++) {
				objNum += hintScanner.hintTable.offsets[i].objNum;
			}
			//Scan the page.
			System.out.println(objNum);
			Page page = new Page(scanner, 
					(PdfDictionary) crossScanner.getObject(new PdfObjectReference(objNum, 0)),
					50, 50);
			System.out.println("Page: " + page.contents);
			return page;
		}
		return null;
	}
	
	public Object getObject(PdfObjectReference reference) {
		return crossScanner.getObject(reference);
	}

}
