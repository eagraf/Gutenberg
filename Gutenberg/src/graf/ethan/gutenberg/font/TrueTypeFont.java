package graf.ethan.gutenberg.font;

import java.util.HashMap;

public class TrueTypeFont {
	
	public CMAP[] cmap;

}

class CMAP {
	int platformID;
	int platformSpecificID;
	int offset;
	
	HashMap<Integer, Integer> map;
}
