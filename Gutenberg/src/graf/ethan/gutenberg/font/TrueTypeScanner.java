package graf.ethan.gutenberg.font;

import java.nio.ByteBuffer;

import graf.ethan.gutenberg.scanner.FileScanner;

public class TrueTypeScanner {
	
	private ByteBuffer buf;
	
	private boolean scalerType;
	private int tableNum;
	private int searchRange;
	private int entrySelector;
	private int rangeShift;
	private TableDirectory[] tableDirectory;
	
	public TrueTypeScanner(ByteBuffer data) {
		this.buf = data;
		scanFontDirectory();
	}
	
	public void scanFontDirectory() {
		buf.position(0);
		if(readInt(4, false) == 0x00010000) {
			scalerType = true;
		}
		tableNum = readInt(2, false);
		searchRange = readInt(2, false);
		entrySelector = readInt(2, false);
		rangeShift = readInt(2, false);
		
		System.out.println("Font Directory");
		System.out.println("Scaler Type: " + scalerType);
		System.out.println("Table Number: " + tableNum);
		System.out.println("Search Range: " + searchRange);
		System.out.println("Entry Selector: " + entrySelector);
		System.out.println("Range Shift: " + rangeShift);
		
		tableDirectory = new TableDirectory[tableNum];
		for(int i = 0; i < tableNum; i ++) {
			tableDirectory[i] = new TableDirectory();
			//Read the 4 character identifier.
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < 4; j ++) {
				sb.append((char) readInt(1, false));
			}
			tableDirectory[i].identifier = sb.toString();
			tableDirectory[i].checkSum = readInt(4, false);
			tableDirectory[i].offset = readInt(4, false);
			tableDirectory[i].length = readInt(4, false);
			
			System.out.println(tableDirectory[i].identifier);
			System.out.println(tableDirectory[i].checkSum);
			System.out.println(tableDirectory[i].offset);
			System.out.println(tableDirectory[i].length);
		}
	}
	
	public int readInt(int bytes, boolean signed) {
		int next = buf.get();
		System.out.println(next);
		int res = next;
		for(int i = 1; i < bytes; i ++) {
			next = buf.get();
			res *= 256;
			res += next;
			System.out.println(next);
		}
		
		if(signed) {
			//Undo the twos complement
			res -= 1;
			res = ~res;
		}
		return res;
	}
	
	

}
