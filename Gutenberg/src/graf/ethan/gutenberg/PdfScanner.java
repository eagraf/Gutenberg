package graf.ethan.gutenberg;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfScanner {
	FileScanner scanner;

	public PdfScanner(FileScanner scanner) {
		this.scanner = scanner;
	}
	/*
	 * Gets the type of the next token
	 *  0: Boolean 1: Integer 2: Float 3: String 4: Name 5: Array 6: Dictionary 7: Stream 8: Null 9: ObjectReference 10: Object 11: Not Object
	 */
	public int nextType() {
		String next = scanner.next();
		char nextCharArray[] = next.toCharArray();
		//scanner.shiftTokenPosition(-1);
		
		if(next == "true" || next == "false") {
			return 0;
		}
		if(scanner.hasNextInt()) {
			//scanner.shiftTokenPosition(1);
			if(scanner.hasNextInt()) {
				//scanner.shiftTokenPosition(1);
				if(scanner.next() == "R") {
					//scanner.shiftTokenPosition(-2);
					return 9;
				}
				if(scanner.next() == "obj") {
					//scanner.shiftTokenPosition(-2);
					return 10;
				}
		//		scanner.shiftTokenPosition(-1);
				return 1;
			}
			return 1;
		}
		if(scanner.hasNextFloat()) {
			return 2;
		}
		if(nextCharArray[0] == '(' && nextCharArray[nextCharArray.length - 1] == ')') {
			return 3;
		}
		if(nextCharArray[0] == '/') {
			return 4;
		}
		if(nextCharArray[0] == '[') {
			return 5;
		}
		if(next == "<<") {
			return 6;
		}
		if(next == "stream") {
			return 7;
		}
		if(next == "null") {
			return 8;
		}
		return 11;
	}
	
	/*
	 * Gets the type of the next token
	 *  0: Boolean 1: Integer or ObjectReference or Object 2: Float 3: String 4: Name 5: Array 6: Dictionary 7: Stream 8: Null 11: Not Object
	 */
	public int getType(String token) {
		char tokenCharArray[] = token.toCharArray();
		scanner.shiftTokenPosition(-1);
		
		if(token == "true" || token == "false") {
			return 0;
		}
		if(tokenCharArray[0] == '(' && tokenCharArray[tokenCharArray.length - 1] == ')') {
			return 3;
		}
		if(tokenCharArray[0] == '/') {
			return 4;
		}
		if(tokenCharArray[0] == '[') {
			return 5;
		}
		if(token == "<<") {
			return 6;
		}
		if(token == "stream") {
			return 7;
		}
		if(token == "null") {
			return 8;
		}
		try {
			Integer.parseInt(token);
			return 1;
		}
		catch(NumberFormatException e) {
			try {
				Float.parseFloat(token);
				return 2;
			}
			catch(NumberFormatException i) {
				return 10;
			}
		}
	}
	
	public Object scanNextObject() throws NotCorrectPdfTypeException {
		String next = scanner.next();
		StringBuilder stringBuilder = new StringBuilder(next);
		int type = nextType();
		
		if(stringBuilder.charAt(0) == '/' && type == 4) {
			stringBuilder.deleteCharAt(0);
		}
		if(stringBuilder.charAt(0) == '(' && stringBuilder.charAt(stringBuilder.length() - 1) == ')' && type == 3) {
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.deleteCharAt(0);
		}
		
		switch(type) {
			case 0:
				boolean bool = Boolean.parseBoolean(stringBuilder.toString());
				return bool;
			case 1:
				return Integer.parseInt(stringBuilder.toString());
			case 2:
				return Float.parseFloat(stringBuilder.toString());
			case 3:
				return stringBuilder.toString();
			case 4:
				return stringBuilder.toString();
			case 5:
				try {
					scanner.shiftTokenPosition(-1);
					return scanArray();
				}
				catch (NotCorrectPdfTypeException e) {
					e.printStackTrace();
				}
			case 6:
				try {
					return scanDictionary();
				}
				catch(NotCorrectPdfTypeException e){
					e.printStackTrace();
				}
			case 8:
				return null;
			case 9:
				try {
					return scanObjectReference();
				}
				catch(NotCorrectPdfTypeException e) {
					e.printStackTrace();
				}
			case 10:
				try {
					return scanObject();
				}
				catch(NotCorrectPdfTypeException e) {
					e.printStackTrace();
				}
			case 11:
				throw new NotCorrectPdfTypeException();
			default:
				throw new NotCorrectPdfTypeException();
		}
	}
	
	/*
	 * like scanNextObject(), can only read single token objects.
	 */
	public Object getObject(String token) throws NotCorrectPdfTypeException {
		StringBuilder stringBuilder = new StringBuilder(token);
		int type = getType(token);
		
		if(stringBuilder.charAt(0) == '/' && type == 4) {
			stringBuilder.deleteCharAt(0);
		}
		if(stringBuilder.charAt(0) == '(' && stringBuilder.charAt(stringBuilder.length() - 1) == ')' && type == 3) {
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.deleteCharAt(0);
		}
		
		switch(type) {
			case 0:
				boolean bool = Boolean.parseBoolean(token);
				return bool;
			case 1:
				return Integer.parseInt(token);
			case 2:
				return Float.parseFloat(token);
			case 3:
				return stringBuilder.toString();
			case 4:
				return stringBuilder.toString();
			case 8:
				return null;
			case 11:
				throw new NotCorrectPdfTypeException();
			default:
				throw new NotCorrectPdfTypeException();
		}
	}
	
	public String scanString() throws NotCorrectPdfTypeException {
		StringBuilder result = new StringBuilder(scanner.next());
		if(result.charAt(0) == '(' && result.charAt(result.length() - 1) == ')') {
			result.deleteCharAt(0);
			result.deleteCharAt(result.length() - 1);
			return result.toString();
		}
		else {
			throw new NotCorrectPdfTypeException();
		}
	}
	
	public String scanName() throws NotCorrectPdfTypeException {
		StringBuilder result = new StringBuilder(scanner.next());
		if(result.charAt(0) == '/') {
			result.deleteCharAt(0);
			return result.toString();
		}
		else {
			throw new NotCorrectPdfTypeException();
		}
	}
	
	public ArrayList<Object> scanArray() throws NotCorrectPdfTypeException {
		ArrayList<Object> result = new ArrayList<Object>();
		StringBuilder entry = new StringBuilder(scanner.next());
		if(entry.charAt(0) == '[') {
			entry.deleteCharAt(0);
			result.add(getObject(entry.toString()));
			while(entry.charAt(entry.length() -1) != ']') {
				result.add(scanNextObject());
			}
			entry.deleteCharAt(entry.length() - 1);
			result.add(getObject(entry.toString()));
			return result;
		}
		
		else {
			throw new NotCorrectPdfTypeException();
		}
	}
	
	public HashMap<String, Object> scanDictionary() throws NotCorrectPdfTypeException {
		try {
			HashMap<String, Object> dictionary = new HashMap<>();
			if(scanner.next() == "<<") {
				while(scanner.next() != ">>") {
					scanner.shiftTokenPosition(-1);
					dictionary.put(scanName(), scanNextObject());
				}
			}
			return dictionary;
		}
		catch(NotCorrectPdfTypeException e) {
			throw new NotCorrectPdfTypeException();
		}
	}
	
	public PdfObject scanObject() throws NotCorrectPdfTypeException {
		int objNum, genNum;
		Object object;
		try {
			if(scanner.hasNextInt()) {
				objNum = scanner.nextInt();
				if(scanner.hasNextInt()) {
					genNum = scanner.nextInt();
					if(scanner.next() == "obj") {
						object = scanNextObject();
						return new PdfObject(objNum, genNum, object);
					}
					else {
						throw new NotCorrectPdfTypeException();
					}
				}
				else {
					throw new NotCorrectPdfTypeException();
				}
			}
			else {
				throw new NotCorrectPdfTypeException();
			}
		}
		catch(NotCorrectPdfTypeException e) {
			throw new NotCorrectPdfTypeException();
		}
	}
	
	public PdfObjectReference scanObjectReference() throws NotCorrectPdfTypeException {
		int objNum, genNum;
		try {
			if(scanner.hasNextInt()) {
				objNum = scanner.nextInt();
				if(scanner.hasNextInt()) {
					genNum = scanner.nextInt();
					if(scanner.next() == "obj") {
						return new PdfObjectReference(objNum, genNum);
					}
					else {
						throw new NotCorrectPdfTypeException();
					}
				}
				else {
					throw new NotCorrectPdfTypeException();
				}
			}
			else {
				throw new NotCorrectPdfTypeException();
			}
		}
		catch(NotCorrectPdfTypeException e) {
			throw new NotCorrectPdfTypeException();
		}
	}
}
