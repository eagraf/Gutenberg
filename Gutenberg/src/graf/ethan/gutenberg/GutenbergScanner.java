package graf.ethan.gutenberg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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
	
	//Drawers
	public GutenbergDrawer gutenbergDrawer;
	
	//Markers
	public long trailerPos;
	public long startXrefPos;
	public ArrayList<CrossReferenceSection> xrefs;
	
	//File Dictionaries
	public HashMap<String, Object> trailer;
	public HashMap<String, Object> catalog;
	public HashMap<String, Object> pageTree;
	
	public GutenbergScanner(File f) {
		fileScanner = new FileScanner(f);
		pdfScanner = new PdfScanner(fileScanner);
		firstPass();
		crossScanner = new CrossReferenceScanner(pdfScanner, xrefs);
		scanCatalog();
		streamScanner = new StreamScanner(this);
	}
	
	public void setDrawer(GutenbergDrawer drawer) {
		this.gutenbergDrawer = drawer;
	}
	
	/*
	 * Finds and marks the locations of important structures.
	 */
	@SuppressWarnings("unchecked")
	public void firstPass() {
		String nextLine = fileScanner.nextLine();
		xrefs = new ArrayList<>();
		while(nextLine != null) {
			switch(nextLine) {
				case TRAILER:
					//Find the trailer
					trailerPos = fileScanner.getPosition();
					pdfScanner.skipWhiteSpace();
					trailer = (HashMap<String, Object>) pdfScanner.scanNext();
					System.out.println("Trailer: " + trailer);
					break;
				case XREF:
					//Find all of the XREF sections
					pdfScanner.skipWhiteSpace();
					int startNum = pdfScanner.scanNumeric().intValue();
					pdfScanner.skipWhiteSpace();
					int length = (int) pdfScanner.scanNumeric().intValue();
					xrefs.add(new CrossReferenceSection(startNum, length, fileScanner.getPosition()));
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
	@SuppressWarnings("unchecked")
	public void scanCatalog() {
		catalog = (HashMap<String, Object>) crossScanner.getObject(((PdfObjectReference) trailer.get("Root")));
		pageTree = (HashMap<String, Object>) crossScanner.getObject(((PdfObjectReference) catalog.get("Pages")));
		System.out.println("Catalog: " + catalog);
		System.out.println("Page Tree: " + pageTree);
	}
	
	/*
	 * Gets a page object to be rendered.
	 */
	@SuppressWarnings("unchecked")
	public Page getPage() {
		HashMap<String, Object> pageObject = (HashMap<String, Object>) crossScanner.getObject((PdfObjectReference) ((ArrayList<Object>) pageTree.get("Kids")).get(0));
		System.out.println("Page Object: " + pageObject);
		//The coordinates are temporary.
		return new Page(this, pageObject, 100, 100);
	}
}
