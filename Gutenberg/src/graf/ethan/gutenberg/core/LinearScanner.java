package graf.ethan.gutenberg.core;

import java.util.ArrayList;

import graf.ethan.gutenberg.hint.HintScanner;
import graf.ethan.gutenberg.pdf.Page;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.scanner.FileScanner;
import graf.ethan.gutenberg.scanner.PdfScanner;
import graf.ethan.gutenberg.scanner.StreamScanner;
import graf.ethan.gutenberg.scanner.XObjectScanner;
import graf.ethan.gutenberg.xref.XrefLinear;
import graf.ethan.gutenberg.xref.XrefSection;
import graf.ethan.gutenberg.xref.XrefStreamScanner;

public class LinearScanner {
	
	//Scanners
	private GutenbergScanner scanner;
	public FileScanner fileScanner;
	public PdfScanner pdfScanner;
	public StreamScanner streamScanner;
	public XObjectScanner xObjectScanner;
	public HintScanner hintScanner;
	
	public XrefLinear crossScanner;
	
	public PdfDictionary params;
	
	@SuppressWarnings("unchecked")
	public LinearScanner(GutenbergScanner scanner, PdfDictionary lpDict) {
		this.scanner = scanner;
		this.fileScanner = scanner.fileScanner;
		this.pdfScanner = scanner.pdfScanner;
		this.streamScanner = scanner.streamScanner;
		this.xObjectScanner = scanner.xObjectScanner;
		this.hintScanner = new HintScanner(scanner); 
		this.crossScanner = new XrefLinear();
		
		this.params = lpDict;
		
		pdfScanner.skipWhiteSpace();
		long xrefPos = fileScanner.getPosition();
		getXref(xrefPos);
		
		hintScanner.setStream(((ArrayList<Number>) params.get("H")).get(0).longValue());
		hintScanner.scanOffsetHeader(((Number) params.get("N")).intValue());
	}
	
	public void getXref(long xrefPos) {
		Object next = pdfScanner.scanNext();
		System.out.println(next);
		if(next == "XREF") {
			//Find all of the XREF sections
			pdfScanner.skipWhiteSpace();
			int startNum = pdfScanner.scanNumeric().intValue();
			pdfScanner.skipWhiteSpace();
			int length = (int) pdfScanner.scanNumeric().intValue();
			this.crossScanner.xRef1.xrefs.add(new XrefSection(startNum, length, fileScanner.getPosition()));
		}		
		if(next.getClass() == PdfDictionary.class) {
			if(((PdfDictionary) next).has("Type")) {
				String type = (String) ((PdfDictionary) next).get("Type");
				System.out.println(type);
				if(type.equals("XRef")) {
					this.crossScanner.xRef1 = new XrefStreamScanner(scanner);
					((XrefStreamScanner) this.crossScanner.xRef1).setStream(xrefPos);
				}
			}
			if(((PdfDictionary) next).has("Prev")) {
				long loc = ((Number) ((PdfDictionary) next).get("Prev")).longValue();
				scanner.fileScanner.setPosition(loc);
				Object nextObj = pdfScanner.scanNext();
				if(nextObj.getClass() == PdfDictionary.class) {
					this.crossScanner.xRef2 = new XrefStreamScanner(scanner);
					((XrefStreamScanner) this.crossScanner.xRef2).setStream(loc);
				}
			}
			if(((PdfDictionary) next).has("Root")) {
				scanner.document.setCatalog((PdfDictionary) ((PdfDictionary) next).get("Root"));
			}
		}
		scanner.crossScanner = this.crossScanner;
	}

	public Page getPage(int num) {
		if(num == 0) {
			Page page = new Page(scanner, 
					(PdfDictionary) crossScanner.getObject(new PdfObjectReference((int) params.get("O"), 0)),
					50, 50);
			System.out.println("Page: " + page.contents);
			return page;
		}
		if(num < ((Number) params.get("N")).intValue()) {
			int objNum = 1;
			for(int i = 1; i < num; i ++) {
				objNum += hintScanner.hintTable.offsets[i].objNum;
			}
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
