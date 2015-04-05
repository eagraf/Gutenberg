package graf.ethan.gutenberg.scanner;

import java.util.ArrayList;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.xref.XrefList;
import graf.ethan.gutenberg.xref.XrefScanner;
import graf.ethan.gutenberg.xref.XrefSection;
import graf.ethan.gutenberg.xref.XrefStreamScanner;

/*
 * This class is used to read the trailer of the PDF file backwards, so that the program does not have to read the
 * whole file to reach the end.
 */
public class InitialScanner {
	
	public GutenbergScanner scanner;
	private FileScanner fileScanner;
	private PdfScanner pdfScanner;
	
	private static final String ENDLINE = "\n\r";
	
	public ArrayList<Long> trailerPositions;
	
	private XrefList xrefList;
	
	private PdfDictionary trailer;
	private boolean firstTrailer = true;
	
	public InitialScanner(GutenbergScanner scanner) {
		this.scanner = scanner;
		this.fileScanner = scanner.fileScanner;
		this.pdfScanner = scanner.pdfScanner;
		
		fileScanner.setPosition(scanner.document.file.length() - 1);
		
		this.xrefList = new XrefList();
	}
	
	/*
	 * Read the previous character.
	 */
	public char prev() {
		char res = fileScanner.nextChar();
		fileScanner.shiftPosition(-2);
		return res;
	}
	
	/*
	 * Read in the line above.
	 */
	public String prevLine() {
		StringBuilder sb = new StringBuilder();
		char next = prev();
		while(!(ENDLINE.indexOf(next) >= 0)) {
			sb.insert(0,  next);
			next = prev();
		}
		//System.out.println(sb.toString());
		return sb.toString();
	}
	
	public long getTrailerPos() {
		boolean fin = false;
		//Get the final trailer.
		while(!fin) {
			String line = prevLine();
			System.out.println(line);
			try {
				//If the string "trailer" is in the line.
				if(line.substring(0, 7).equals("trailer")) {
					System.out.println("Trailer Pos: " + fileScanner.getPosition());
					return (long) fileScanner.getPosition() - 1;
				} 
			}
			catch(StringIndexOutOfBoundsException e) {
				//Pass
			}
		}
		return -1;
	}
	
	public PdfDictionary getTrailer() {
		return trailer;
	}
	
	/*
	 * Scans all of the trailers, based on the location of the last trailer.
	 */
	public void scanTrailers(long firstPos) {
		fileScanner.setPosition(firstPos);
		//Skip to the correct position.
		pdfScanner.scanNext();
		pdfScanner.scanNext();
		pdfScanner.scanNext();
		//Find the position of the last xref section.
		if(((String) pdfScanner.scanNext()).equals("STARTXREF")) {
			long pos = ((Number) pdfScanner.scanNext()).longValue();
			boolean fin = false;
			//Scan all of the xref sections.
			while(!fin) {
				pos = scanXrefSection(pos);		
				if(pos == -1) {
					System.out.println("fin");
					fin = true;
				}
			}
		}
	}
	
	/*
	 * Scan a cross reference section. Returns the prev trailer entry if it exists.
	 */
	public long scanXrefSection(long pos) {
		scanner.fileScanner.setPosition(pos);
		Object next = pdfScanner.scanNext();
		System.out.println(next);
		//Scan a normal xref section, or a cross reference stream.
		if(next.getClass() == String.class) {
			if(((String) next).equals("XREF")) {
				return scanXref();
			}
		}
		//Cross reference stream.
		if(next.getClass() == PdfDictionary.class) {
			return scanXrefStream((PdfDictionary) next, pos);
		}
		return -1;
	}
	
	/*
	 * Scan a normal xref section.
	 */
	public long scanXref() {
		Object next;
		ArrayList<XrefSection> sections = new ArrayList<>();
		while(true) {
			next = pdfScanner.scanNext();
			int start, len;
			//Scan in the parameters for xref sections, the start number and length.
			if(next.getClass() == Integer.class) {
				start = (int) next;
				System.out.println(next);
				len = (int) pdfScanner.scanNext();
				System.out.println(len);
				pdfScanner.skipWhiteSpace();
				sections.add(new XrefSection(start, len, fileScanner.getPosition()));
				fileScanner.shiftPosition(20 * len);
			}
			//Once the xref is scanned, scan the trailer.
			else if(next.getClass() == String.class) {
				if(next.equals("TRAILER")) {
					xrefList.addXref(new XrefScanner(scanner, sections));
					PdfDictionary trailer = (PdfDictionary) pdfScanner.scanNext();
					System.out.println("Trailer: " + trailer);
					//If this is the first trailer scanned (i.e. at the end of the file), record it as the actual trailer.
					if(firstTrailer) {
						this.trailer = trailer;
						firstTrailer = false;
					}
					//If there is a previous entry, return its location so it can be scanned too.
					if(trailer.has("Prev")) {
						return ((Number) trailer.get("Prev")).longValue();
					}
					else {
						return -1;
					}
				}
			}
		}
	}
	
	/*
	 * Scan a cross reference stream.
	 */
	public long scanXrefStream(PdfDictionary next, long pos) {
		//Record the trailer if it is the final one.
		if(firstTrailer) {
			this.trailer = (PdfDictionary) next;
			firstTrailer = false;
		}
		//Make sure that this is a cross reference stream.
		if(((PdfDictionary) next).has("Type")) {
			String type = (String) ((PdfDictionary) next).get("Type");
			System.out.println(type);
			if(type.equals("XRef")) {
				//Create the XrefStreamScanner.
				XrefStreamScanner res = new XrefStreamScanner(scanner);
				pdfScanner.skipWhiteSpace();
				res.setStream(pos);
				xrefList.addXref(res);
			}
		}
		//Return the location of the prev entry.
		if(((PdfDictionary) next).has("Prev")) {
			return ((Number) ((PdfDictionary) next).get("Prev")).longValue();
		}
		else {
			return -1;
		}
	}
	
	public XrefList getXref() {
		return xrefList;
	}
}
