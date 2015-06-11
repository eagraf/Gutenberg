package graf.ethan.gutenberg.font;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class TrueTypeFont {
	
	public TableHead head;
	public TableCmap[] cmap;
	public TableCmap uCmap;
	public TableCVT cvt;
	public TableMaxP maxp;
	
	public int charCount;
	
	/*
	 * Find the preferred unicode cmap to be used for the font.
	 */
	public void setUnicodeCmap() {
		for(int i = 0; i < cmap.length; i ++) {
			//There should only be one unicode cmap in the font. If there is one, use it.
			if(cmap[i].platformID == 0) {
				uCmap = cmap[i];
				break;
			}
			//Otherwise use a unicode cmap in the Windows platform specific encoidng.
			if(cmap[i].platformID == 3) {
				if(cmap[i].platformSpecificID == 1 || cmap[i].platformSpecificID == 10) {
					uCmap = cmap[i];
				}
			}
		}
	}
}

/*
 * Represents the mappings between character codes and glyph indices.
 */
class TableCmap {
	int platformID;
	int platformSpecificID;
	int offset;
	
	HashMap<Integer, Integer> map;
}

/*
 * The header table of the font file.
 */
class TableHead {
	public float version;
	public float fontRevision;
	public long checkSumAdjustment;
	public long magicNumber;
	
	public boolean[] flags;
	
	public int unitsPerEm;
	
	public long created;
	public long modified;
	
	public int xMin;
	public int yMin;
	public int xMax;
	public int yMax;
	
	public boolean[] macStyle;
	
	public int lowestRecPPEM;
	public int fontDirectionHint;
	
	public int indexToLocFormat;
	public int glyphDataFormat;
}


/*
 * Represents the Glyph Table.
 */
class TableGlyph {
	public int contourNum;
	public int xMin;
	public int yMin;
	public int xMax;
	public int yMax;
	
	public int pointNum;
	
	public int[] contourEnds;
	public int instructionLen;
	public ArrayList<Integer> instructions;
	public int[] flags;
	
	public int xCoords[];
	public int yCoords[];
	
	public int getInstruction(int pointer) {
		return instructions.get(pointer);
	}
}

/*
 * Represents an entry into the table directory of a TrueType font
 */
class TableDirectory {
	public String identifier;
	public long checkSum;
	public long offset;
	public long length;
}

/*
 * The CVT Variations Table.
 */
class TableCvar {
	public int version;
	public int tupleCount;
	public int offsetToData;
	public int pointNumbers[];
	public int tupleData[];
}

/*
 * The CVT Table
 */
class TableCVT {
	public int controlValues[];
}

/*
 * Maximum Profile Table
 */
class TableMaxP {
	public int version;
	public int numGlyphs;
	public int maxPoints;
	public int maxContours;
	public int maxComponentPoints;
	public int maxComponentContours;
	public int maxZones = 2;
	public int maxTwilightPoints;
	public int maxStorage;
	public int maxFunctionDefs;
	public int maxInstructionDefs;
	public int maxStackElements;
	public int maxSizeOfInstructions;
	public int maxComponentElements;
	public int maxComponentDepth;
}

/*
 * Represents a point.
 */
class Point {
	float x;
	float y;
	boolean onCurve;
	
	public Point() {
		
	}
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
}

class PointF26Dot6 {
	int x;
	int y;
	boolean onCurve;
	boolean touchX = false;
	boolean touchY = false;
	
	public PointF26Dot6() {
		
	}
	
	public PointF26Dot6(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

class PointF2Dot14 {
	short x;
	short y;
	boolean onCurve;
	
	public PointF2Dot14() {
		
	}
	
	public PointF2Dot14(short x, short y) {
		this.x = x;
		this.y = y;
	}

}