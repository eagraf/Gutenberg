package graf.ethan.gutenberg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FileScanner {
	public int length;
	public int lineNumber;
	public RandomAccessFile reader;
	public LineNumberReader lineReader;
	public FileChannel fileChannel;
	public byte[] buffer = new byte[1];
	
	private static final int EOF = -1;
	private static final String DELIMITER = " \t\f\r\n";   
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	
	public FileScanner(File f) {
		length = (int) f.length();
		try {
			reader = new RandomAccessFile(f, "r");
			fileChannel = reader.getChannel();
			lineReader = new LineNumberReader(new FileReader(f));
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String next() {
		StringBuffer res = new StringBuffer();
		
		skipWhiteSpace();
		
		try {
			while(!isWhiteSpace((char) buffer[0]) && fileChannel.position() <= length) {
				res.append((char) buffer[0]);
				reader.read(buffer, 0, 1);
			}
			return res.toString();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
			return null;
		}
	}
	
	public boolean hasNextBoolean() {
		try {
			Boolean.parseBoolean(next());
			shiftPosition(-1);
			return true;
		}
		catch(NumberFormatException e) {
			return false;
		}
	}
	
	public boolean hasNextInt() {
		try {
			Integer.parseInt(next());
			shiftPosition(-1);
			return true;
		}
		catch(NumberFormatException e) {
			return false;
		}
	}
	
	public boolean hasNextShort() {
		try {
			Short.parseShort(next());
			shiftPosition(-1);
			return true;
		}
		catch(NumberFormatException e) {
			return false;
		}
	}
	
	public boolean hasNextLong() {
		try {
			Long.parseLong(next());
			shiftPosition(-1);
			return true;
		}
		catch(NumberFormatException e) {
			return false;
		}
	}
	
	public boolean hasNextFloat() {
		try {
			Float.parseFloat(next());
			shiftPosition(-1);
			return true;
		}
		catch(NumberFormatException e) {
			return false;
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
	
	public String nextLine() {
		try {
			return lineReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
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
	
	public boolean isWhiteSpace (char character) {
		return DELIMITER.indexOf((char)character) >= 0;
	}
	
	public int getLineNumber() {
		return lineReader.getLineNumber();
	}
	
	public long getPosition() {
		try {
			return fileChannel.position();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			fileChannel.position(fileChannel.position() + offset);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
