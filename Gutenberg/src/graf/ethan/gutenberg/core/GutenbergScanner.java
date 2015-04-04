package graf.ethan.gutenberg.core;

import graf.ethan.gutenberg.font.FontStreamScanner;
import graf.ethan.gutenberg.pdf.Page;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfDocument;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.scanner.FileScanner;
import graf.ethan.gutenberg.scanner.PdfScanner;
import graf.ethan.gutenberg.scanner.InitialScanner;
import graf.ethan.gutenberg.scanner.StreamScanner;
import graf.ethan.gutenberg.scanner.XObjectScanner;
import graf.ethan.gutenberg.xref.Xref;
import graf.ethan.gutenberg.xref.XrefScanner;
import graf.ethan.gutenberg.xref.XrefSection;

import java.io.File;
import java.util.ArrayList;

/*
 * The main scanner class for Gutenberg, responsible for navigating the PDF's basic structure.
 * Contains instances of FileScanner, PdfScanner, CrossReferenceScanner, and StreamScanner
 */
public class GutenbergScanner {
	//Keywords
	private static final String XREF = "xref";
	private static final String STARTXREF = "startxref";
	private static final String TRAILER = "trailer";
	
	//Scanners
	public FileScanner fileScanner;
	public PdfScanner pdfScanner;
	public Xref crossScanner;
	public StreamScanner streamScanner;
	public XObjectScanner xObjectScanner;
	public LinearScanner linearScanner;
	public InitialScanner reverseScanner;
	public FontStreamScanner fontScanner;
	
	//Drawers
	public GutenbergDrawer gutenbergDrawer;
	
	//Markers
	public long trailerPos;
	public long startXrefPos;
	public ArrayList<XrefSection> xrefs;
	
	public PdfDocument document;
	
	public GutenbergScanner(File f) {
		this.document = new PdfDocument(f);
		this.fileScanner = new FileScanner(f);
		this.pdfScanner = new PdfScanner(this);
		this.xObjectScanner = new XObjectScanner(this);
		this.streamScanner = new StreamScanner(this);
		this.fontScanner = new FontStreamScanner(this);
		
		
		
		Object first = scanFirst();
		
		//Procedure for normal documents.
		if(!document.linearized) {
			//Reverse scanner scans xref and trailer sections.
			this.reverseScanner = new InitialScanner(this);
			reverseScanner.scanTrailers(reverseScanner.getTrailerPos());
			PdfDictionary trailer = reverseScanner.getTrailer();
			this.crossScanner = reverseScanner.getXref();
			trailer.setCrossScanner(this.crossScanner);
			document.setTrailer(trailer);
			//Next step of the scanning process.
			scanCatalog();
		}
		//Procedure for Linearized PDF files.
		else {
			System.out.println("Linearized");
			this.linearScanner = new LinearScanner(this, (PdfDictionary) first);
		}
	}
	
	public void setDrawer(GutenbergDrawer drawer) {
		this.gutenbergDrawer = drawer;
	}
	
	/*
	 *Scan the first object of the file to determine whether it is linearized.
	 *Return true if the trailer does not have to be read. 
	 */
	public Object scanFirst() {
		Object nextObj = pdfScanner.scanNext();
		System.out.println(nextObj);
		if(nextObj.getClass() == PdfDictionary.class) {
			if(((PdfDictionary) nextObj).has("Linearized")) {
				document.linearized = true;
			}
		}
		return nextObj;
	}
	
	/*
	 * Scans the file catalog, which contains important information about the PDF.
	 */
	public void scanCatalog() {
		document.setCatalog((PdfDictionary) document.getTrailer().get("Root"));
		document.setPageTree((PdfDictionary) document.getCatalog().get("Pages"));
		System.out.println("Catalog: " + document.getCatalog());
		System.out.println("Page Tree: " + document.getPageTree());
	}
	
	/*
	 * Method for early stage cross reference reading.
	 */
	public Object getObject(PdfObjectReference reference) {
		if(document.linearized) {
			return linearScanner.getObject(reference);
		}
		else {
			return crossScanner.getObject(reference);
		}
	}
	
	/*
	 * Gets a page object to be rendered.
	 */
	@SuppressWarnings("unchecked")
	public Page getPage(int num) {
		if(document.linearized) {
			return linearScanner.getPage(num);
		}
		if(num < ((ArrayList<Object>) document.getPageTree().get("Kids")).size()) {
			PdfDictionary pageObject = (PdfDictionary) document.getPages().get(num);
			System.out.println("Page Object: " + pageObject);
			//The coordinates are temporary.
			return new Page(this, pageObject, 50, 50);
		}
		return null;
	}
}
