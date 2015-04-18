package graf.ethan.gutenberg.font;

import java.util.HashMap;

public class TrueTypeFont {
	
	public TableHead head;
	public TableCmap[] cmap;
	public TableCmap uCmap;
	
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
