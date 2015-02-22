package graf.ethan.gutenberg.misc;

import java.awt.Font;
import java.util.HashMap;

public class GlyphCache {
	
	public HashMap<Font, HashMap<Character, HashMap<Float, Glyph>>> cache;
	
	public GlyphCache() {
		cache = new HashMap<Font, HashMap<Character, HashMap<Float, Glyph>>>();
	}
	
	public Glyph get(Font font, char c, float f) {
		return cache.get(font).get(c).get(f);
	}
	
	public void put(Font font, char c, float f, Glyph path) {
		if(cache.containsKey(font)) {
			if(cache.containsKey(c)) {
				cache.get(font).get(c).put(f, path);
			}
			else {
				cache.get(font).put(c, new HashMap<Float, Glyph>());
				cache.get(font).get(c).put(f, path);
			}
		}
		else {
			cache.put(font, new HashMap<Character, HashMap<Float, Glyph>>());
			cache.get(font).put(c, new HashMap<Float, Glyph>());
			cache.get(font).get(c).put(f, path);
		}
	}
	
	public boolean has(Font font, char c, float f) {
		if(cache.containsKey(font)) {
			if(cache.get(font).containsKey(c)) {
				if(cache.get(font).get(c).containsKey(f)) {
					return true;
				}
				return false;
			}
			return false;
		}
		return false;
	}
	
	public void clear() {
		cache = new HashMap<Font, HashMap<Character, HashMap<Float, Glyph>>>();
	}
}
