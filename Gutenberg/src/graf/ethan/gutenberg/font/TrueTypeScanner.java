package graf.ethan.gutenberg.font;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class TrueTypeScanner {
	
	private ByteBuffer buf;
	
	private boolean scalerType;
	private int tableNum;
	private int searchRange;
	private int entrySelector;
	private int rangeShift;
	private HashMap<String, TableDirectory> tableDirectory;
	
	public TrueTypeScanner(ByteBuffer data) {
		this.buf = data;
		scanFontDirectory();
	}
	
	public void scanFontDirectory() {
		buf.position(0);
		if(readInt(4, false) == 0x00010000) {
			scalerType = true;
		}
		tableNum = (int) readInt(2, false);
		searchRange = (int) readInt(2, false);
		entrySelector = (int) readInt(2, false);
		rangeShift = (int) readInt(2, false);
		
		System.out.println("Font Directory");
		System.out.println("Scaler Type: " + scalerType);
		System.out.println("Table Number: " + tableNum);
		System.out.println("Search Range: " + searchRange);
		System.out.println("Entry Selector: " + entrySelector);
		System.out.println("Range Shift: " + rangeShift);
		
		tableDirectory = new HashMap<>();
		for(int i = 0; i < tableNum; i ++) {
			TableDirectory entry = new TableDirectory();
			//Read the 4 character identifier.
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < 4; j ++) {
				sb.append((char) readInt(1, false));
			}
			entry.identifier = sb.toString();
			entry.checkSum = readInt(4, false);
			entry.offset = readInt(4, false);
			entry.length = readInt(4, false);
			
			tableDirectory.put(entry.identifier, entry);
			
			System.out.println(entry.identifier);
			System.out.println(entry.checkSum);
			System.out.println(entry.offset);
			System.out.println(entry.length);
		}
	}
	
	public void getCMAP() {
		
	}
	
	public long readInt(int bytes, boolean signed) {
		long res = buf.get() & 0xFF;
		for(int i = 1; i < bytes; i ++) {
			res *= 256;
			res += buf.get() & 0xFF;
		}
		
		if(signed) {
			//Undo the twos complement
			res -= 1;
			res = ~res;
		}
		return res;
	}
	
	

}
