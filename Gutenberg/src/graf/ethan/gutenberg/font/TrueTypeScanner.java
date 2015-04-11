package graf.ethan.gutenberg.font;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
		getCmap((int) tableDirectory.get("cmap").offset);
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
	
	public void getCmap(int offset) {
		//Set the location to the position of the cmap table.
		buf.position(offset);
		for(int j = offset; j < offset+100; j ++) {
			System.out.println(buf.get(j));
		}
		int version = (int) readInt(2, false);
		int subTableNum = (int) readInt(2, false);
		
		CodingTable[] maps = new CodingTable[subTableNum];
		
		System.out.println("Version: " + version);
		System.out.println("Sub-Table Number: " + subTableNum);
		
		for(int i = 0; i < subTableNum; i ++) {
			maps[i] = new CodingTable();
			maps[i].platformID = (int) readInt(2, false);
			maps[i].platformSpecificID = (int) readInt(2, false);
			maps[i].offset = (int) readInt(4, false);
			
			System.out.println(maps[i].platformID);
			switch(maps[i].platformID) {
			case 0:
				System.out.println("Unicode Platform");
				System.out.println("Platform Specific ID: " + maps[i].platformSpecificID);
				System.out.println("Offset: " + maps[i].offset);
				break;
			case 1:
				System.out.println("Macintosh Platform");
				System.out.println("Platform Specific ID: " + maps[i].platformSpecificID);
				System.out.println("Offset: " + maps[i].offset);
				break;
			case 2:
				System.out.println("Reserved Platform");
				System.out.println("Platform Specific ID: " + maps[i].platformSpecificID);
				System.out.println("Offset: " + maps[i].offset);
				break;
			case 3:
				System.out.println("Windows Platform");
				System.out.println("Platform Specific ID: " + maps[i].platformSpecificID);
				System.out.println("Offset: " + maps[i].offset);
				break;
			}
		}
		for(int i = 0; i < subTableNum; i ++) {
			maps[i].map = scanCmapTable(offset+maps[i].offset);
		}
		
	}
	
	/*
	 * Actually map the encodings to the glyph indices.
	 */
	public HashMap<Integer, Integer> scanCmapTable(int offset) {
		System.out.println("Offset: " + offset);
		buf.position(offset);
		
		int format = (int) readInt(2, false);
		int length = (int) readInt(2, false);
		int language = (int) readInt(2, false);
		
		//A HashMap is returned, with the key being the character code, and the value being the glyph indice.
		HashMap<Integer, Integer> res;
		
		System.out.println("Format: " + format);
		switch(format) {
		case 0:
			//Format 0: 256 single byte character codes mapped to 256 single byte glyph indices. Rarely used.
			res = new HashMap<Integer, Integer>();
			for(int i = 0; i < 256; i ++) {
				int value =(int) readInt(1, false);
				res.put(value, i);
				System.out.println(value + ", " + i);
			}
			return res;
		case 4:
			//Format 4: Used for non-contiguous stretches of glyph encodings.
			int segCountX2 = (int) readInt(2, false);
			int searchRange = (int) readInt(2, false);
			int entrySelector = (int) readInt(2, false);
			int rangeShift = (int) readInt(2, false);
			
			int[] endCode = readArray(2, false, segCountX2/2);
			int reservePad = (int) readInt(2, false);
			int[] startCode = readArray(2, false, segCountX2/2);
			int[] idDelta = readArray(2, false, segCountX2/2);
			int[] idRangeOffset = readArray(2, false, segCountX2/2);
			break;
		case 6:
		case 12:
		}
		return null;
	}
	
	public int[] readArray(int bytes, boolean signed, int num) {
		int[] res = new int[num];
		for(int i = 0; i < num; i ++) {
			res[i] = (int) readInt(bytes, signed);
		}
		return res;
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

class CodingTable {
	int platformID;
	int platformSpecificID;
	int offset;
	
	HashMap<Integer, Integer> map;
}
