package graf.ethan.gutenberg;

/*
 * A class that keeps important information for stream objects.
 */
public class PdfStream {
	//Important location variables
	public long length;
	public long startPos;
	public long endPos;
	
	public GraphicsState state;
	
	public PdfStream(long startPos, long length) {
		this.startPos = startPos;
		this.length = length;
		this.endPos = startPos + length - 1;
		
		//The Graphics State is unique to each stream
		this.state = new GraphicsState();
	}

}
