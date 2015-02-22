package graf.ethan.gutenberg.pdf;

/*
 * A class that keeps important information for stream objects.
 */
public class PdfStream {
	//Important location variables
	public long length;
	public long startPos;
	public long endPos;
	
	public PdfStream(long startPos, long length) {
		this.startPos = startPos;
		this.length = length;
		this.endPos = startPos + length - 1;
	}

}
