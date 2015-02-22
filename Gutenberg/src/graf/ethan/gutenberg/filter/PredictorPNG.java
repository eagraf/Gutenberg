package graf.ethan.gutenberg.filter;

public class PredictorPNG extends Predictor {
	
	private FilterFlate filter;
	
	private int colors;
	private int bpc;
	private int columns;
	
	private int linePos = 0;
	private int offset = 0;
	private int byteWidth;
	private int bpp;
	private int[] currLine;
	private int[] prevLine;
	
	public PredictorPNG(FilterFlate filter, int colors, int bpc, int columns) {
		this.filter = filter;
		
		this.colors = colors;
		this.bpc = bpc;
		this.columns = columns;
		
		bpp = (int) Math.ceil((this.bpc * this.colors)/8.0);
		byteWidth = (int) Math.ceil(bpp * this.columns);
		
		currLine = nextLine();
	}
	
	/*
	 * Get the next component.
	 * A component is a specific value for a color within a sample.
	 * Components are usually associated with images, but are also used for regular streams.
	 */
	public int nextComponent() {
		//If the end of a line has been reached, set the current line to the next one.
		if(linePos == byteWidth) {
			prevLine = currLine;
			linePos = 0;
			currLine = nextLine();
		}
		//If all components have been read from a byte, get the next one.
		int next = 0;
		if(offset == 8) {
			offset = 0;
			next = currLine[linePos];
			linePos ++;
		}
		if(offset == 16) {
			offset = 0;
			next = currLine[linePos];
			linePos += 2;
		}
		//Get the value of the component based off of the bits per component and the current offset.
		int res = 0;
		switch(bpc) {
			case 1:
				res = (next >> (7 - offset)) & 1;
				break;
			case 2:
				res = (next >> (6 - offset)) & 3;
				break;
			case 4:
				res = (next >> (4 - offset)) & 15;
				break;
			case 8:
				res = next;
				break;
			case 16:
				int nextByte = filter.read();
				res = (256 * (int) (next & 0xFF)) + (int) (nextByte & 0xFF);
				break;
		}
		offset += bpc;
		return res;
	}
	
	/*
	 * Get the next line for a predictor algorithm.
	 */
	public int[] nextLine() {
		int[] line = null;
		int algorithm = filter.read();
		System.out.println("Algorithm: " + algorithm);
		switch(algorithm) {
			case 0:
				//Use no predictor.
				line = getNoneLine();
				break;
			case 1:
				//Predict from the component to the left.
				line = getSubLine();
				break;
			case 2:
				//Predict from the component above.
				line = getUpLine();
				break;
			case 3:
				//Predict from the average of the component to the left and the component above.
				line = getAvgLine();
				break;
			case 4:
				//Paeth algorithm not implemented yet.
				break;
		}
		return line;
	}
	
	/*
	 * No prediction.
	 */
	public int[] getNoneLine() {
		int[] line = new int[byteWidth];
		for(int i = 0; i < byteWidth; i ++) {
			line[i] = filter.read();
		}
		return line;
	}
	
	/*
	 * Get the next line, predicting from the value to the left with the same color.
	 */
	public int[] getSubLine() {
		int[] line = new int[byteWidth];
		for(int i = 0; i < byteWidth; i ++) {
			if(i - bpp >= 0) {
				line[i] = (filter.read() + line[i - bpp]) % 256;
			}
			else {
				line[i] = filter.read() % 256;
			}
		}
		return line;
	}
	
	/*
	 * Get the next line, predicting from the value above.
	 */
	public int[] getUpLine() {
		int[] line = new int[byteWidth];
		if(prevLine != null) {
			for(int i = 0; i < byteWidth; i ++){
				line[i] = (filter.read() + prevLine[i]) % 256;
			}
		}
		else {
			for(int i = 0; i < byteWidth; i ++){		
				line[i] = filter.read() % 256;
			}
		}
		return line;
	}
	
	/*
	 * Get the next line, predicting from the average of the value to the left and the value above.
	 */
	public int[] getAvgLine() {
		int[] line = new int[byteWidth];
		for(int i = 0; i < byteWidth; i ++) {
			int raw = 0;
			int prior = 0;
			if(i - bpp >= 0) {
				prior = line[i - bpp];
			}
			if(prevLine != null) {
				prior = prevLine[i];
			}
			line[i] = (filter.read() +((int) Math.floor((raw + prior)/2.0)) % 256);
		}
		return line;
	}

	@Override
	public boolean finished() {
		if(filter.inf.finished() && linePos == byteWidth) {
			return true;
		}
		return false;
	}

	@Override
	public long skip(long n) {
		long count = 0;
		int lines = (int) Math.floor(n / byteWidth);
		for(int i = 0; i < lines; i ++) {
			nextLine();
			
		}
		int spaces = (int) (n % byteWidth);
		if(linePos + spaces >= byteWidth) {
			nextLine();
			count += byteWidth;
			spaces -= byteWidth - linePos;
		}
		count += spaces;
		linePos += spaces;
		
		return count;
	}
	
	@Override
	public void reset() {
		filter.reset();
		currLine = nextLine();
		prevLine = null;
	}
	
	
	/*
	 * public int[] getPaethLine() 
	 */

}
