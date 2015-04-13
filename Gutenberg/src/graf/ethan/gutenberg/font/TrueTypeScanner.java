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
	
	private TrueTypeFont font;
	
	public TrueTypeScanner(ByteBuffer data) {
		this.buf = data;
		
		font = new TrueTypeFont();
		
		scanFontDirectory();
		font.cmap = getCmap((int) tableDirectory.get("cmap").offset);
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
	
	public CMAP[] getCmap(int offset) {
		//Set the location to the position of the cmap table.
		buf.position(offset);

		int version = (int) readInt(2, false);
		int subTableNum = (int) readInt(2, false);
		
		CMAP[] maps = new CMAP[subTableNum];
		
		System.out.println("Version: " + version);
		System.out.println("Sub-Table Number: " + subTableNum);
		
		for(int i = 0; i < subTableNum; i ++) {
			maps[i] = new CMAP();
			maps[i].platformID = (int) readInt(2, false);
			maps[i].platformSpecificID = (int) readInt(2, false);
			maps[i].offset = (int) readInt(4, false);
		}
		for(int i = 0; i < subTableNum; i ++) {
			maps[i].map = scanCmapTable(offset+maps[i].offset);
		}
		return maps;
	}
	
	/*
	 * Actually map the encodings to the glyph indices.
	 */
	@SuppressWarnings("unused")
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
				int value = (int) readInt(1, false);
				res.put(i, value);
				System.out.println(i + ", " + value);
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
			int off1 = buf.position();
			int[] idRangeOffset = readArray(2, false, segCountX2/2);
			
			res = new HashMap<Integer, Integer>();
			//Map every possible 2 byte character code.
			for(int c = 0; c < 65536; c ++) {
				boolean done = false;
				int i = 0;
				while(!done) {
					//Find the first endcode that is greater or equal to the character code to be mapped.
					if(endCode[i] >= c) {
						//If the corresponding startcode is less than or equal to the character code:
						if(startCode[i] <= c) {
							//If idRangeOffset does not equal 0, use this equation to find the glyph index address:
							//glyphIndexAddress = idRangeOffset[i] + 2 * (c - startCode[i]) + (Ptr) &idRangeOffset[i]
							if(idRangeOffset[i] != 0) {
								//glyphIndexAddress = idRangeOffset[i] + 2 * (c - startCode[i]) + (Ptr) &idRangeOffset[i]
								int off2 = off1 + (2 * i);
								int glyphIndexAddress = idRangeOffset[i] + (2 * (c - startCode[i])) + off2;
								System.out.println("Address: " + glyphIndexAddress);
								
								//Read the glyph index.
								buf.position(glyphIndexAddress);
								int glyphIndex = (int) readInt(2, false);
								
								int value = 0;
								//Add glyphIndex to idDelta.
								if(glyphIndex != 0) {
									value = (glyphIndex + idDelta[i]) % 35536;
								}
								res.put(c, value);
								System.out.println(c + ", " + value);
							}
							//Otherwise, just add the character code and idDelta.
							else {
								int value = (idDelta[i] + c) % 65536;
								res.put(c, value);
								System.out.println(c + ", " + value);
							}
						}
						else {
							res.put(c, 0);
							//System.out.println(i + ", 0f");
						}
						done = true;
					}
					//Otherwise, increment.
					i ++;
				}
			}
			return res;
		case 6:
		case 12:
		}
		return null;
	}
	
	/*
	 * Reads in an array of integers instead of just one.
	 */
	public int[] readArray(int bytes, boolean signed, int num) {
		int[] res = new int[num];
		for(int i = 0; i < num; i ++) {
			res[i] = (int) readInt(bytes, signed);
		}
		return res;
	}
	
	/*
	 * Read an integer from the buffer. Parameters for number of bytes and whether it is signed.
	 */
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
	
	/*
	 * Return the font.
	 */
	public TrueTypeFont getFont() {
		return font;
	}
}
