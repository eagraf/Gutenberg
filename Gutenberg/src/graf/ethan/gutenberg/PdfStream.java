package graf.ethan.gutenberg;

/*
 * A class that keeps important information for stream objects.
 */
public class PdfStream {
	//Important location variables
	public long length;
	public long startPos;
	public long endPos;
	
	//Text state variables
	float charSpace = 0;
	float wordSpace = 0;
	float scale = 100;
	float leading = 0;
	float textRise = 0;
	float textKnockout;
	int renderMode = 0;
	int fontSize;
	String font;
	
	public PdfStream(long startPos, long length) {
		this.startPos = startPos;
		this.length = length;
		
		this.endPos = startPos + length - 1;
	}

}
