package graf.ethan.gutenberg.scanner;

import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfObject;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.pdf.PdfOperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * PdfScanner reads in PDF objects, keywords and operators.
 */

public class PdfScanner {
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
	
	public FileScanner scanner;
	public GutenbergScanner gScanner;
	
	public PdfScanner(GutenbergScanner scanner) {
		this.gScanner = scanner;
		this.scanner = gScanner.fileScanner;
	}
	
	/*
	 * Scans the next object in the file.
	 */
	public Object scanNext() {
		skipWhiteSpace();
		char next = scanner.nextChar();
		//Checks to see if next is an opening delimiter. If so, the appropriate method is called.
		if(DELIMITEROPEN.indexOf(next) >= 0) {
			switch(next) {
				//Strings (String)
				case '(':
					String resString = scanString();
					return resString;
				//Hex-string <12AE3BC2930>
				case '<':
					next = scanner.nextChar();
					//If there is a second '<', then the next object is a dictionary.
					if(next == '<') {
						return scanDictionary();
					}
					else {
						scanner.shiftPosition(-1);
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
			long pos1 = scanner.getPosition() - 1;
			scanner.shiftPosition(-1);
			//See if the object being scanned is a number or a reference(2 0 R)
			skipWhiteSpace();
			try {
				int num1 = scanner.nextInt();
				int num2 = scanner.nextInt();
				long pos2 = scanner.getPosition();
				if(scanner.nextChar() == 'R') {
					return new PdfObjectReference(num1, num2);
				}
				else {
					scanner.setPosition(pos2);
				}
				if(scanKeyword() == 2) {
					Object obj = scanNext();
					scanKeyword();
					return obj;
				}
				else {
					scanner.setPosition(pos1);
					return scanNumeric();
				}
			}
			catch(NumberFormatException e) {
				scanner.setPosition(pos1);
				return scanNumeric();
			}			
		}
		else {
			scanner.shiftPosition(-1);
			int keyWord = scanKeyword();
			//Returns for keywords need to be added. Most of these won't be used.
			switch(keyWord) {
				case 0:
					return false;
				case 1:
					return true;
				case 2:
					return "OBJ";
				case 3:
					return "ENDOBJ";
				case 4:
					return "STREAM";
				case 5:
					return "ENDSTREAM";
				case 6:
					return "XREF";
				case 7:
					return "STARTXREF";
				case 8:
					return "TRAILER";
				case 9:
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
		char next = scanner.nextChar();
		StringBuilder keyword = new StringBuilder();
		while(!isWhiteSpace(next) && !(DELIMITER.indexOf(next) >= 0)) {
			keyword.append(next);
			next = scanner.nextChar();
		}
		scanner.shiftPosition(-1);
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
 		char next = scanner.nextChar();
 		while(NUMERAL.indexOf(next) >= 0) {
 			if(next == '.') {
 				isFloat = true;
 			}
 			res.append(next);
 			next = scanner.nextChar();
 		}
 		scanner.shiftPosition(-1);
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
 		char next = scanner.nextChar();
 		while(NUMERAL.indexOf(next) >= 0) {
 			res.append(next);
 			next = scanner.nextChar();
 		}
 		scanner.shiftPosition(-1);
 		return Long.parseLong(res.toString());
 	}
	
	/*
	 * Scans in a string. The opening parenthesis was already read in.
	 */
	public String scanString() {
		StringBuilder res = new StringBuilder();
		char next = scanner.nextChar();
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
		while(true) {
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
			else if((DELIMITER.indexOf(next) >= 0) || isWhiteSpace(next)) {
				scanner.shiftPosition(-1);
				//System.out.println(res.toString());
				return res.toString();
			}
			else {
				res.append(next);
			}
			next = scanner.nextChar();
		}
	}
	
	/*
	 * Read everything from the comment until the newline.
	 */
	public void scanComment() {
		char next = scanner.nextChar();
 		while(next != '\n' && next != '\r') {
			next = scanner.nextChar();
		}
 		scanner.shiftPosition(-1);
	}
	
	/*
	 * Scans in an array object.
	 */
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
	
	/*
	 * Scans in a dictionary.
	 */
	public PdfDictionary scanDictionary() {
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
		return new PdfDictionary(res, gScanner);
	}
	
	/*
	 * Scans in an indirect object.
	 */
	public PdfObject scanObject() {
		PdfObject res;
		Object object;
		int objectNumber;
		int generationNumber;
		
		skipWhiteSpace();
		objectNumber = scanNumeric().intValue();
		skipWhiteSpace();
		generationNumber = scanNumeric().intValue();
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
		//Skip comments too
		while(isWhiteSpace(next) || next == '%') {
			if(next == '%') {
				scanComment();
				next = scanner.nextChar();
			}
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