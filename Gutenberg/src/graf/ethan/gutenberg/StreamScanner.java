package graf.ethan.gutenberg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * Similar to PdfScanner, but does not have random access to file. Reads in data through a filter.
 */
public class StreamScanner {
	//White-space and delimiter characters in PDF
	private static final String WHITESPACE = " \0\t\n\f\r";
	private static final String DELIMITER = "()<>[]{}/%";
	private static final String DELIMITEROPEN = "(<[{/%";
	private static final String NUMERAL = "0123456789.+-";
	private static final String HEX = "0123456789ABCDEFabcdef";
	
	private static final String[] OPERATOR = {"b", "B", "b*", "B*", 
		"BDC", "BI", "BMC", "BT", 
		"BX", "c", "cm", "CS", 
		"cs", "d", "d0", "d1", 
		"Do", "DP", "EI", "EMC",
		"ET", "EX", "f", "F", 
		"f*", "G", "g", "gs",
		"h", "i", "ID", "j", 
		"J", "K", "k", "l",
		"m", "M", "MP", "n",
		"q", "Q", "re", "RG",
		"rg", "ri", "s", "S",
		"SC", "sc", "SCN", "scn",
		"sh", "T*", "Tc", "Td",
		"TD", "Tf", "Tj", "TJ",
		"TL", "Tm", "Tr", "Ts",
		"Tw", "Tz", "v", "w",
		"W", "W*", "y", "\'", "\""};
	
	//PDF keywords
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String OBJ = "obj";
	private static final String ENDOBJ = "endobj";
	private static final String STREAM = "stream";
	private static final String ENDSTREAM = "endstream";
	private static final String XREF = "xref";
	private static final String STARTXREF = "startxref";
	private static final String TRAILER = "trailer";
	private static final String REFERENCE = "R";
	
	public long startPos;
	public long length;
	long byteCount = 0;
	
	public String filterName = "Default";
	public Filter filter;
	
	public GutenbergScanner scanner;
	
	public StreamScanner(GutenbergScanner scanner) {
		this.scanner = scanner;
	}
	
	/*
	 * Scans in PDF objects until an operator is reached, at which point a PDF Operation will be returned.
	 */
	public PdfOperation nextOperation() {
		ArrayList<Object> args = new ArrayList<>();
		while(!finished()) {
			Object next = scanNext();
			if(next == null) {
				return null;
			}
			else if(next.getClass() == PdfOperator.class) {
				return new PdfOperation((PdfOperator) next, args);
			}
			args.add(next);
		}
		return null;
	}
	
	public boolean finished() {
		if(filter.getClass() == Filter.class) {
			try {
				if(filter.fis.getChannel().position() - filter.startPos < filter.length) {
					return true;
				}
				else {
					return false;
				}
			} catch (IOException e) {
				return false;
			}
		}
		if(filter.getClass() == FilterFlate.class) {
			if(((FilterFlate) filter).inf.finished()) {
				return true;
			}
			else {
				return false;
			}
		}
		if(filter.getClass() == FilterDCT.class) {
			return ((FilterDCT) filter).finished;
		}
		return false;
	}
	
	/*
	 * Set the current stream.
	 */
	public void setStream(PdfObjectReference reference) {
		scanner.fileScanner.setPosition(scanner.crossScanner.getObjectPosition(reference));
		
		//Scan in the stream dictionary
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		PdfDictionary streamDictionary;
		
		if(scanner.pdfScanner.scanKeyword() == 2) {
			streamDictionary = (PdfDictionary) scanner.pdfScanner.scanNext();
			System.out.println("Stream Dictionary: " + streamDictionary);
			length = ((Number) streamDictionary.get("Length")).longValue();
			if(streamDictionary.has("Filter")) {
				filterName = (String) streamDictionary.get("Filter");
			}
		}
		else {
			streamDictionary = null;
		}
		
		//Begin the scanning process
		scanner.pdfScanner.scanKeyword();
		scanner.pdfScanner.skipWhiteSpace();
		startPos = scanner.fileScanner.getPosition();
		switch(filterName) {
			case "Default":
				filter = new Filter(startPos, length, scanner.fileScanner.file);
				break;
			case "FlateDecode":
				filter = new FilterFlate(startPos, length, scanner.fileScanner.file);
				break;
			case "DCTDecode":
				filter = new FilterDCT(startPos, length, scanner.fileScanner.file);
				break;
		}
	}
	
	public char nextChar() {
		byteCount ++;
		return filter.nextChar();
	}
	
	public void back() {
		filter.back();
		byteCount --;
	}
	
	/*
	 * Scans the next object in the file.
	 */
	public Object scanNext() {
		skipWhiteSpace();
		char next = nextChar();
		//Checks to see if next is an opening delimiter. If so, the appropriate method is called.
		if(DELIMITEROPEN.indexOf(next) >= 0) {
			switch(next) {
				//Strings (String)
				case '(':
					String resString = scanString();
					return resString;
				//Hex-string <12AE3BC2930>
				case '<':
					next = nextChar();
					//If there is a second '<', then the next object is a dictionary.
					if(next == '<') {
						return scanDictionary();
					}
					else {
						back();
						return scanHexString();
					}
				//Comment %Comment
				case '%':
					scanComment();
					break;
				//Array [2 4 45.3 true 2 0 R]
				case '[':
					return scanArray();
				//Name /Name
				case '/':
					return scanName();
				default:
					return null;
			}	
		}
		if(NUMERAL.indexOf(next) >= 0) {
			back();
			skipWhiteSpace();
			return scanNumeric();
		}
		else {
			back();
			int keyWord = scanKeyword();
			//Returns for keywords need to be added. Most of these won't be used.
			switch(keyWord) {
				case 0:
					return false;
				case 1:
					return true;
				case 2:
					//scanObject
				case 4:
					//scanStream
				case 5:
					return null;
				case 6:
					//scanXref
				case 7:
					//scanTrailer
				case 8:
					return null;
				default:
					if(keyWord >= 32 && keyWord < 105) {
						return new PdfOperator(keyWord - 32);
					}
			}
		}
		return null;
	}
	
	/*
	 * Returns an integer that denotes the type of keyword. 10 signifies not a keyword.
	 */
	public int scanKeyword() {
		skipWhiteSpace();
		char next = nextChar();
		//System.out.println(next);
		StringBuilder keyword = new StringBuilder();
		while(!isWhiteSpace(next) && !(DELIMITER.indexOf(next) >= 0)) {
			//System.out.println(next);
			keyword.append(next);
			next = nextChar();
		}
		//System.out.println(keyword.toString());
		back();
		switch(keyword.toString()) {
			case FALSE:
				return 0;
			case TRUE:
				return 1;
			case OBJ:
				return 2;
			case ENDOBJ:
				return 3;
			case STREAM:
				return 4;
			case ENDSTREAM:
				return 5;
			case XREF:
				return 6;
			case STARTXREF:
				return 7;
			case TRAILER:
				return 8;
			case REFERENCE:
				return 9;
			default:
				int index = Arrays.asList(OPERATOR).indexOf(keyword.toString());
				if(index >= 0) {
					return index + 32;
				}
				return 10;
		}
	}
	
	/*
	 * Scans in a numeric object, either an Integer(int) of Real(float).
	 */
 	public Number scanNumeric() {
 		boolean isFloat = false;
 		StringBuilder res = new StringBuilder();
 		char next = nextChar();
 		while(NUMERAL.indexOf(next) >= 0) {
 			if(next == '.') {
 				isFloat = true;
 			}
 			res.append(next);
 			next = nextChar();
 		}
 		back();
 		if(isFloat) {
 			return (float) Float.parseFloat(res.toString());
 		}
 		else {
 			Long l = Long.parseLong(res.toString());
 			if(l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
 				return (long) l;
 			}
 			else {
 				return (int) Integer.parseInt(res.toString());
 			}
 		}	
 	}
 	
 	/*
 	 *Scans a long number
 	 */
 	public Number scanLong() {
 		StringBuilder res = new StringBuilder();
 		char next = nextChar();
 		while(NUMERAL.indexOf(next) >= 0) {
 			res.append(next);
 			next = nextChar();
 		}
 		back();
 		return Long.parseLong(res.toString());
 	}
	
	/*
	 * Scans in a string. The opening parenthesis was already read in.
	 */
	public String scanString() {
		StringBuilder res = new StringBuilder();
		char next = nextChar();
		int parenthesis = 0;
		while(next != ')' || parenthesis > 0) {
			//If parentheses are balanced, they are valid. THIS NEEDS TO BE FIXED.
			if(next == '(') {
				parenthesis ++;
			}
			if(next == ')') {
				parenthesis --;
			}
			//Reading in escape sequences
			if(next == '\\') {
				next = nextChar();
				//Octal character codes
				if(next >= '0' && next <= '7') {
					StringBuilder octalEscape = new StringBuilder(Character.toString(next));
					next = nextChar();
					if(next >= '0' && next <= '7') {
						octalEscape.append(next);
						next = nextChar();
						if(next >= '0' && next <= '7') {
							octalEscape.append(next);
						}
					}
					/* TEMPORARY SOLUTION: DOES NOT RETURN CORRECT STANDARD FOR TEXT ENCODING*/
					res.append((char) Integer.parseInt(octalEscape.toString()));
				}
				//Special escape sequences 
				else {
					switch(next) {
						case 'n':
							res.append('\n');
							break;
						case 'r':
							res.append('\r');
							break;
						case 't':
							res.append('\t');
							break;
						case 'b':
							res.append('\b');
							break;
						case 'f':
							res.append('\f');
							break;
						case '(':
							res.append('(');
							break;
						case ')':
							res.append(')');
							break;
						case '\\':
							res.append('\\');
							break;
						case '\r':
							next = nextChar();
							if(next == '\n') {
								break;
							}
							res.append('\r');
							break;
					}
				}
			}
			else {
				res.append(next);
			}
			next = nextChar();
		}
		
		return res.toString();
	}
	
	/*
	 * Scans in a string represented by two hexadecimal numerals. Skips non-hex characters and appends a 0 
	 * at the end if there are an odd number of hex numerals.
	 */
	public String scanHexString() {
		StringBuilder res = new StringBuilder();
		StringBuilder hex = new StringBuilder();
		char next = nextChar();
		while(next != '>') {		
			if(HEX.indexOf((char)next) >= 0) {
				hex.append(next);
			}
			next = nextChar();
		}
		
		 for( int i=0; i<hex.length()-1; i+=2 ){
		      String s = hex.substring(i, (i + 2));
		      byte n = (byte) Integer.parseInt(s, 16);
		      res.append((char) n);
		      //System.out.println((char) n);
		  }
		  
		 return res.toString();
	}
	
	/*
	 * Scans in a name denoted by a '/' preceding it. 
	 */
	public String scanName() {
		StringBuilder res = new StringBuilder();
		char next = nextChar();
		while(true) {
			if(next == '#') {
				StringBuilder hex = new StringBuilder();
				next = nextChar();
				if(HEX.indexOf(next) >= 0) {
					hex.append(next);
					next = nextChar();
					if(HEX.indexOf(next) >= 0) {
						hex.append(next);
					}
					res.append((char) Integer.parseInt(hex.toString(), 16));
				}
				if(hex.length() == 0) {
					res.append('#');
					res.append(next);
				}
			}
			else if((DELIMITER.indexOf(next) >= 0) || isWhiteSpace(next)) {
				back();
				//System.out.println(res.toString());
				return res.toString();
			}
			else {
				res.append(next);
			}
			next = nextChar();
		}
	}
	
	/*
	 * Read everything from the comment until the newline.
	 */
	public void scanComment() {
		char next = nextChar();
 		while(next != '\n' && next != '\r') {
			next = nextChar();
		}
 		back();
	}
	
	/*
	 * Scans in an array object.
	 */
	public ArrayList<Object> scanArray() { 
		ArrayList<Object> res = new ArrayList<>();
		skipWhiteSpace();
		char next = nextChar();
		while(next != ']') {
			back();
			res.add(scanNext());
			skipWhiteSpace();
			next = nextChar();
		}
		
		return res;
	}
	
	/*
	 * Scans in a dictionary.
	 */
	public PdfDictionary scanDictionary() {
		HashMap<String, Object> res = new HashMap<>();
		skipWhiteSpace();
		char next = nextChar();
		while(next != '>') {
			String key;
			if(next == '/') {
				key = scanName();
			}
			else {
				key = "NO_KEY";
			}
			Object value = scanNext();
			skipWhiteSpace();
			res.put(key, value);
			next = nextChar();
		}
		nextChar();
		return new PdfDictionary(res, scanner);
	}
	
	/* 
	 * Skip white-space characters until the next object
	 */
	public void skipWhiteSpace() {
		char next = nextChar();
		//Skip comments too
		while(isWhiteSpace(next) || next == '%') {
			if(next == '%') {
				scanComment();
				next = nextChar();
			}
			next = nextChar();
		}
		back();
	}
	
	/*
	 * Simple function for determining whether a character is white-space.
	 */
	public boolean isWhiteSpace(char character) {
		return WHITESPACE.indexOf((char)character) >= 0;
	}
}
