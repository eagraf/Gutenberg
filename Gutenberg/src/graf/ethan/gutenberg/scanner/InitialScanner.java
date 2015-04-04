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
	
	public char prev() {
		char res = fileScanner.nextChar();
		fileScanner.shiftPosition(-2);
		return res;
	}
	
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
		PdfDictionary res = null;
		fileScanner.setPosition(firstPos);
		pdfScanner.scanNext();
		pdfScanner.scanNext();
		pdfScanner.scanNext();
		if(((String) pdfScanner.scanNext()).equals("STARTXREF")) {
			long pos = ((Number) pdfScanner.scanNext()).longValue();
			boolean fin = false;
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
		System.out.println("POS: " + pos);
		scanner.fileScanner.setPosition(pos);
		Object next = pdfScanner.scanNext();
		System.out.println(next);
		if(next.getClass() == String.class) {
			
			if(((String) next).equals("XREF")) {
				
				ArrayList<XrefSection> sections = new ArrayList<>();
				while(true) {
					next = pdfScanner.scanNext();
					int start, len;
					if(next.getClass() == Integer.class) {
						
						start = (int) next;
						System.out.println(next);
						len = (int) pdfScanner.scanNext();
						System.out.println(len);
						pdfScanner.skipWhiteSpace();
						sections.add(new XrefSection(start, len, fileScanner.getPosition()));
						fileScanner.shiftPosition(20 * len);
					}
					else if(next.getClass() == String.class) {
						if(next.equals("TRAILER")) {
							System.out.println("XREF Section: " + pos);
							xrefList.addXref(new XrefScanner(scanner, sections));
							PdfDictionary trailer = (PdfDictionary) pdfScanner.scanNext();
							System.out.println("Trailer: " + trailer);
							if(firstTrailer) {
								this.trailer = trailer;
								firstTrailer = false;
							}
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
		}
		if(next.getClass() == PdfDictionary.class) {
			if(firstTrailer) {
				this.trailer = (PdfDictionary) next;
				firstTrailer = false;
			}
			if(((PdfDictionary) next).has("Type")) {
				String type = (String) ((PdfDictionary) next).get("Type");
				System.out.println(type);
				if(type.equals("XRef")) {
					XrefStreamScanner res = new XrefStreamScanner(scanner);
					pdfScanner.skipWhiteSpace();
					res.setStream(pos);
					xrefList.addXref(res);
				}
			}
			if(((PdfDictionary) next).has("Prev")) {
				return ((Number) ((PdfDictionary) next).get("Prev")).longValue();
			}
			else {
				return -1;
			}
		}
		return -1;
	}
	
	public XrefList getXref() {
		return xrefList;
	}
}
