package graf.ethan.gutenberg;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfScanner {
	//White-space and delimiter characters in PDF
	private static final String WHITESPACE = " \0\t\n\f\r";
	private static final String DELIMITER = "()<>[]{}/%";
	
	//PDF keywords
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
	
	public void skipWhiteSpace() {
		char next = scanner.nextChar();
		while(isWhiteSpace(next)) {
			next = scanner.nextChar();
		}
		scanner.shiftPosition(-1);
	}
	
	public boolean isWhiteSpace(char character) {
		return WHITESPACE.indexOf((char)character) >= 0;
	}
	
	/*
	 * Scans in a string. The opening parenthesis was already read in.
	 */
	public String scanString() {
		StringBuilder res = new StringBuilder();
		char next = scanner.nextChar();
		while(next != ')') {
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
	
	public String scanHexString() {
		String res = new String();
		return res;
	}
	
	public String scanName() {
		String res = new String();
		return res;
	}
	
	public ArrayList<Object> scanArray() {
		ArrayList<Object> res = new ArrayList<>();
		return res;
	}
	
	public HashMap<String, Object> scanDictionary() {
		HashMap<String, Object> res = new HashMap<>();
		return res;
	}
	
	
	
}