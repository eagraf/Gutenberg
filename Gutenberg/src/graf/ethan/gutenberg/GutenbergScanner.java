package graf.ethan.gutenberg;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;

public class GutenbergScanner {
	//Keywords
	private static final String XREF = "xref";
	private static final String STARTXREF = "startxref";
	private static final String TRAILER = "trailer";
	
	public FileScanner fileScanner;
	public PdfScanner pdfScanner;
	
	public long trailerPos;
	public long startXrefPos;
	public HashMap<Integer, Long> xrefs;
	
	public GutenbergScanner(File f) {
		fileScanner = new FileScanner(f);
		pdfScanner = new PdfScanner(fileScanner);
	}
	
	public void getVersion() {
		
	}
	
	public void firstPass() {
		String nextLine = fileScanner.nextLine();
		xrefs = new HashMap<Integer, Long>();
		int xCount = 0;
		while(nextLine != null) {
			switch(nextLine) {
				case TRAILER:
					trailerPos = fileScanner.getPosition();
					pdfScanner.skipWhiteSpace();
					HashMap trailer = (HashMap) pdfScanner.scanNext();
					System.out.println(trailer);
					break;
				case XREF:
					xrefs.put(xCount, fileScanner.getPosition());
					xCount ++;
					break;
				case STARTXREF:
					startXrefPos = fileScanner.getPosition();
					break;
				}
			nextLine = fileScanner.nextLine();
		}
	}
}
