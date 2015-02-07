package graf.ethan.gutenberg;


import java.awt.font.GlyphMetrics;
import java.awt.geom.GeneralPath;

public class Glyph {
	
	//Glyph Identifiers
    GlyphMetrics metrics;
    GeneralPath path;
    
    public Glyph(GeneralPath path, GlyphMetrics metrics) {
    	this.path = path;
    	this.metrics = metrics;
    }
}
