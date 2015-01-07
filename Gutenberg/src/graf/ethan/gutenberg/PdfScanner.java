package graf.ethan.gutenberg;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * PdfScanner reads in PDF objects, keywords and operators.
 */

public class PdfScanner {
	//White-space and delimiter characters in PDF
	private static final String WHITESPACE = " \0\t\n\f\r";
	private static final String DELIMITER = "()<>[]{}/%";
	private static final String DELIMITEROPEN = "(<[{/%";
	private static final String DELIMITERCLOSE = ")>]}";
	private static final String NUMERAL = "0123456789.+-";
	private static final String HEX = "0123456789ABCDEFabcdef";
	
	//PDF keywords
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String OBJ = "obj";
	private static final String ENDOBJ = "endobj";
	private static final String STREAM = "stream";
	private static final String ENDSTREAM = "endstream";
	private static final String XREF = "xref";
	private static final String TRAILER = "trailer";
	
	private FileScanner scanner;
	
	public PdfScanner(FileScanner scanner) {
		this.scanner = scanner;
	}
	
	public Object scanNext() {
		skipWhiteSpace();
		char next =  scanner.nextChar();
		if(DELIMITEROPEN.indexOf(next) >= 0) {
			switch(next) {
				case '(':
					String resString = scanString();
					return resString;
				case '<':
					next = scanner.nextChar();
					if(next == '<') {
						return scanDictionary();
					}
					else {
						scanner.shiftPosition(-1);
						return scanHexString();
					}
				case '%':
					scanComment();
					break;
				case '[':
					return scanArray();
				case '/':
					return scanName();
				default:
					return null;
			}	
		}
		if(NUMERAL.indexOf(next) >= 0) {
			scanner.shiftPosition(-1);
			return scanNumeric();
		}
		else {
			scanner.shiftPosition(-1);
			int keyWord = scanKeyword();
			switch(keyWord) {
				case 0:
					return false;
				case 1:
					return true;
				case 2:
					//scanObject
				case 4:
					//scanStream
				case 6:
					//scanXref
				case 7:
					//scanTrailer
				case 8:
					return null;
			}
		}
		return null;
	}
	
	/*
	 * Returns an integer that denotes the type of keyword. 8 signifies not a keyword.
	 */
	public int scanKeyword() {
		String next = scanner.next();
		switch(next) {
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
			case TRAILER:
				return 7;
			default:
				return 8;
		}
	}
	
	/*
	 * Scans in a numeric object, either an Integer(int) of Real(float).
	 */
 	public Number scanNumeric() {
 		boolean isFloat = false;
 		StringBuilder res = new StringBuilder();
 		char next = scanner.nextChar();
 		while(NUMERAL.indexOf(next) >= 0) {
 			if(next == '.') {
 				isFloat = true;
 			}
 			res.append(next);
 			next = scanner.nextChar();
 		}
 		if(isFloat) {
 			return Float.parseFloat(res.toString());
 		}
 		else {
 			return Integer.parseInt(res.toString());
 		}	
 	}
	
	/*
	 * Scans in a string. The opening parenthesis was already read in.
	 */
	public String scanString() {
		StringBuilder res = new StringBuilder();
		char next = scanner.nextChar();
		int parenthesis = 0;
		while(next != ')' || parenthesis > 0) {
			//If parentheses are balanced, they are valid.
			if(next == '(') {
				parenthesis ++;
			}
			if(next == ')') {
				parenthesis --;
			}
			//Reading in escape sequences
			if(next == '\\') {
				next = scanner.nextChar();
				//Octal character codes
				if(next >= '0' && next <= '7') {
					StringBuilder octalEscape = new StringBuilder(Character.toString(next));
					next = scanner.nextChar();
					if(next >= '0' && next <= '7') {
						octalEscape.append(next);
						next = scanner.nextChar();
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
						next = scanner.nextChar();
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
			next = scanner.nextChar();
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
		char next = scanner.nextChar();
		while(next != '>') {		
			if(HEX.indexOf((char)next) >= 0) {
				if(hex.length() >= 2) {
					res.append((char) Integer.parseInt(hex.toString(), 16));
					hex = new StringBuilder();
					if(HEX.indexOf((char)next) >= 0) {
						hex.append(next);
					}
				}
				else {
					hex.append(next);
				}
			}
			next = scanner.nextChar();
		}
		if(hex.length() == 1) {
			hex.append('0');
		}
		res.append((char) Integer.parseInt(hex.toString(), 16));
		return res.toString();
	}
	
	/*
	 * Scans in a name denoted by a '/' preceding it. 
	 */
	public String scanName() {
		StringBuilder res = new StringBuilder();
		char next = scanner.nextChar();
		while(!isWhiteSpace(next)) {
			if(next == '#') {
				StringBuilder hex = new StringBuilder();
				next = scanner.nextChar();
				if(HEX.indexOf(next) >= 0) {
					hex.append(next);
					next = scanner.nextChar();
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
			else if(!(DELIMITER.indexOf(next) >= 0)) {
				res.append(next);
			}
			else {
				return res.toString();
			}
			next = scanner.nextChar();
		}
		return res.toString();
	}
	
	/*
	 * Read everything from the comment until the newline.
	 */
	public void scanComment() {
		char next = scanner.nextChar();
		while(next != '\n') {
			next = scanner.nextChar();
		}
	}
	
	public ArrayList<Object> scanArray() { 
		ArrayList<Object> res = new ArrayList<>();
		skipWhiteSpace();
		char next = scanner.nextChar();
		while(next != ']') {
			scanner.shiftPosition(-1);
			res.add(scanNext());
			skipWhiteSpace();
			next = scanner.nextChar();
		}
		return res;
	}
	
	public HashMap<String, Object> scanDictionary() {
		HashMap<String, Object> res = new HashMap<>();
		skipWhiteSpace();
		char next = scanner.nextChar();
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
			next = scanner.nextChar();
		}	
		
		scanner.nextChar();
		return res;
	}
	
	public PdfObject scanObject() {
		PdfObject res;
		Object object;
		int objectNumber;
		int generationNumber;
		
		skipWhiteSpace();
		objectNumber = (int) scanNumeric();
		skipWhiteSpace();
		generationNumber = (int) scanNumeric();
		skipWhiteSpace();
		if(scanKeyword() == 2) {
			object = scanNext();
		}
		else {
			object = null;
		}
		skipWhiteSpace();
		if(scanKeyword() == 3) {
			res = new PdfObject(objectNumber, generationNumber, object);
		}
		else {
			res = null;
		}
		return res;
	}
	
	/* 
	 * Skip white-space characters until the next object
	 */
	public void skipWhiteSpace() {
		char next = scanner.nextChar();
		while(isWhiteSpace(next)) {
			next = scanner.nextChar();
		}
		scanner.shiftPosition(-1);
	}
	
	/*
	 * Simple function for determining whether a character is white-space.
	 */
	public boolean isWhiteSpace(char character) {
		return WHITESPACE.indexOf((char)character) >= 0;
	}
}