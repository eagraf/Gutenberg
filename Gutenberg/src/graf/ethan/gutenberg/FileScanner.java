package graf.ethan.gutenberg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/*
 * FileScanner is an alternative to the Java Scanner class for reading tokens in a file
 * FileScanner allows for random access file reading while Scanner can only read files sequentially.
 */

public class FileScanner {
	public int length;
	public int lineNumber;
	public RandomAccessFile reader;
	public LineNumberReader lineReader;
	public FileChannel fileChannel;
	public byte[] buffer = new byte[1];
	
	//Delimiters are characters that FileScanner skips over
	private static final String DELIMITER = " \t\f\r\n";   
	
	public FileScanner(File f) {
		length = (int) f.length();
		try {
			reader = new RandomAccessFile(f, "r");
			fileChannel = reader.getChannel();
			lineReader = new LineNumberReader(new FileReader(f));
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	/*
	 * Reads in the next token as a string. Skips over any whitespace characters preceding it.
	 */
	public String next() {
		StringBuffer res = new StringBuffer();
		skipWhiteSpace();
		try {
			res.append((char) buffer[0]);
			while(fileChannel.position() <= length - 1) {
				reader.read(buffer, 0, 1);
				if(!isWhiteSpace((char) buffer[0])) {
					res.append((char) buffer[0]);
				}
				else { 
					break;
				}
			}
			return res.toString();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
			return null;
		}
	}
	
	/*
	 * The "next" functions return the next token of a certain type.
	 */
	public char nextChar() {
		try {
			reader.read(buffer, 0, 1);
			return (char) buffer[0];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	public boolean nextBoolean() {
		return Boolean.parseBoolean(next());
	}
	
	public int nextInt() {
		return Integer.parseInt(next());
	}
	
	public float nextFloat() {
		return Float.parseFloat(next());
	}
	
	public short nextShort() {
		return Short.parseShort(next());
	}
	
	public long nextLong() {
		return Long.parseLong(next());
	}
	
	/*
	 * Reads in the next line (to LineReader, not RandomAccessFile).
	 */
	public String nextLine() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Function that skips over whitespace characters.
	 */
	public void skipWhiteSpace() {
		
		try{
			reader.read(buffer, 0, 1);
			while(isWhiteSpace((char) buffer[0]) && fileChannel.position() < length ) {
				reader.read(buffer, 0, 1);
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void skipPrevWhiteSpace() {
		try{
			shiftPosition(-1);
			reader.read(buffer, 0, 1);
			while(isWhiteSpace((char) buffer[0]) && fileChannel.position() >= 2) {
				shiftPosition(-2);
				reader.read(buffer, 0, 1);			
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public boolean isWhiteSpace (char character) {
		return DELIMITER.indexOf((char)character) >= 0;
	}
	
	/*
	 * Functions for setting and getting position within the file.
	 */
	public int getLineNumber() {
		return lineReader.getLineNumber();
	}
	
	public long getPosition() {
		try {
			return fileChannel.position();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public void setLineNumber(int number) {
		lineReader.setLineNumber(number);
	}
	
	public void setPosition(long newPosition) {
		try {
			fileChannel.position(newPosition);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shiftPosition(long offset) {
		try {
			long newPosition = fileChannel.position() + offset;
			fileChannel.position(newPosition);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}