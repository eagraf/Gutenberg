package graf.ethan.gutenberg;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

public class GutenbergScanner {
	//Keywords
	private static final String XREF = "xref";
	private static final String STARTXREF = "startxref";
	private static final String TRAILER = "trailer";
	
	public FileScanner fileScanner;
	public PdfScanner pdfScanner;
	public CrossReferenceScanner crossScanner;
	public StreamScanner streamScanner;
	
	public long trailerPos;
	public long startXrefPos;
	public ArrayList<CrossReferenceSection> xrefs;
	
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
	
	public void getVersion() {
		
	}
	
	public void firstPass() {
		String nextLine = fileScanner.nextLine();
		xrefs = new ArrayList<>();
		while(nextLine != null) {
			switch(nextLine) {
				case TRAILER:
					trailerPos = fileScanner.getPosition();
					pdfScanner.skipWhiteSpace();
					trailer = (HashMap) pdfScanner.scanNext();
					System.out.println(trailer);
					break;
				case XREF:
					pdfScanner.skipWhiteSpace();
					int startNum = pdfScanner.scanNumeric().intValue();
					pdfScanner.skipWhiteSpace();
					int length = (int) pdfScanner.scanNumeric().intValue();
					xrefs.add(new CrossReferenceSection(startNum, length, fileScanner.getPosition()));
					break;
				case STARTXREF:
					startXrefPos = fileScanner.getPosition();
					break;
				}
			nextLine = fileScanner.nextLine();
		}
		
	}
	
	public void scanCatalog() {
		catalog = (HashMap<String, Object>) crossScanner.getObject(((PdfObjectReference) trailer.get("Root")));
		pageTree = (HashMap<String, Object>) crossScanner.getObject(((PdfObjectReference) catalog.get("Pages")));
		System.out.println(catalog);
		System.out.println(pageTree);
		
		//System.out.println(crossScanner.getObject(((PdfObjectReference) catalog.get("Pages"))));
	}
	
	public Page getPage() {
		HashMap<String, Object> pageObject = (HashMap) crossScanner.getObject((PdfObjectReference) ((ArrayList) pageTree.get("Kids")).get(0));
		
		return new Page(this, pageObject, 100, 100);
	}
	
	public ArrayList<Integer> getMediaBox(HashMap<String, Object> node) {
		ArrayList<Integer> rect;
		if(node.containsKey("MediaBox")) {
			rect = (ArrayList) node.get("MediaBox");
		}
		else {
			rect = getMediaBox((HashMap<String, Object>) crossScanner.getObject((PdfObjectReference) node.get("Parent")));
		}
		return rect;
	}
}
