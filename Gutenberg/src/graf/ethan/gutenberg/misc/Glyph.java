package graf.ethan.gutenberg.misc;


import java.awt.font.GlyphMetrics;
import java.awt.geom.GeneralPath;

public class Glyph {
	
	//Glyph Identifiers
    public GlyphMetrics metrics;
    public GeneralPath path;
    
    public Glyph(GeneralPath path, GlyphMetrics metrics) {
    	this.path = path;
    	this.metrics = metrics;
    }
}
