package graf.ethan.gutenberg;

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
	public CrossReferenceScanner crossScanner;
	public StreamScanner streamScanner;
	public XObjectScanner xObjectScanner;
	
	//Drawers
	public GutenbergDrawer gutenbergDrawer;
	
	//Markers
	public long trailerPos;
	public long startXrefPos;
	public ArrayList<CrossReferenceSection> xrefs;
	
	//File Dictionaries
	public PdfDictionary trailer;
	public PdfDictionary catalog;
	public PdfDictionary pageTree;
	
	public GutenbergScanner(File f) {
		this.fileScanner = new FileScanner(f);
		this.pdfScanner = new PdfScanner(this);
		this.xObjectScanner = new XObjectScanner(this);
		this.streamScanner = new StreamScanner(this);
		firstPass();
		scanCatalog();	
	}
	
	public void setDrawer(GutenbergDrawer drawer) {
		this.gutenbergDrawer = drawer;
	}
	
	/*
	 * Finds and marks the locations of important structures.
	 */
	public void firstPass() {
		String nextLine = fileScanner.nextLine();
		xrefs = new ArrayList<>();
		while(nextLine != null) {
			switch(nextLine) {
				case TRAILER:
					//Find the trailer
					trailerPos = fileScanner.getPosition();
					pdfScanner.skipWhiteSpace();
					trailer = (PdfDictionary) pdfScanner.scanNext();
					System.out.println("Trailer: " + trailer);
					break;
				case XREF:
					//Find all of the XREF sections
					pdfScanner.skipWhiteSpace();
					int startNum = pdfScanner.scanNumeric().intValue();
					pdfScanner.skipWhiteSpace();
					int length = (int) pdfScanner.scanNumeric().intValue();
					xrefs.add(new CrossReferenceSection(startNum, length, fileScanner.getPosition()));
					crossScanner = new CrossReferenceScanner(pdfScanner, xrefs);
					break;
				case STARTXREF:
					//Find the startxref marker at the end of the file.
					startXrefPos = fileScanner.getPosition();
					break;
				}
			nextLine = fileScanner.nextLine();
		}
	}
	
	/*
	 * Scans the file catalog, which contains important information about the PDF.
	 */
	public void scanCatalog() {
		catalog = (PdfDictionary) trailer.get("Root");
		pageTree = (PdfDictionary) catalog.get("Pages");
		System.out.println("Catalog: " + catalog);
		System.out.println("Page Tree: " + pageTree);
	}
	
	/*
	 * Gets a page object to be rendered.
	 */
	@SuppressWarnings("unchecked")
	public Page getPage(int num) {
		if(num < ((ArrayList<Object>) pageTree.get("Kids")).size()) {
			PdfDictionary pageObject = (PdfDictionary) crossScanner.getObject((PdfObjectReference) ((ArrayList<Object>) pageTree.get("Kids")).get(num));
			System.out.println("Page Object: " + pageObject);
			//The coordinates are temporary.
			return new Page(this, pageObject, 50, 50);
		}
		return null;
	}
}
