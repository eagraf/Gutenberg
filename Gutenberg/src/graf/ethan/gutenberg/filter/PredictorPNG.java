package graf.ethan.gutenberg.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class PredictorPNG extends Predictor {
	
	private PredictorFilter filter;
	
	private int colors;
	private int bpc;
	private int columns;
	
	private int linePos = 0;
	private int offset = 0;
	private int byteWidth;
	private int bpp;
	
	private int[] currLine;
	private int[] prevLine;
	private int nextInt;
	
	private boolean skipLine = false;
	
	public PredictorPNG(PredictorFilter filter, int colors, int bpc, int columns) {
		this.filter = filter;
		
		this.colors = colors;
		this.bpc = bpc;
		this.columns = columns;
		
		bpp = (int) Math.ceil((this.bpc * this.colors)/8.0);
		byteWidth = (int) Math.ceil(bpp * this.columns);
		
		currLine = nextLine();
		nextInt = currLine[0];
		linePos++;
	}
	
	/*
	 * Get the next component.
	 * A component is a specific value for a color within a sample.
	 * Components are usually associated with images, but are also used for regular streams.
	 */
	public int nextComponent() {
		//If the end of a line has been reached, set the current line to the next one.
		if(linePos == byteWidth) {
			linePos = 0;
			offset = 8;
			prevLine = currLine;
			currLine = nextLine();
		}
		//If all components have been read from a byte, get the next one.
		if(offset == 8) {		
			offset = 0;
			nextInt = currLine[linePos];
			linePos ++;
		}
		if(offset == 16) {
			offset = 0;
			nextInt = currLine[linePos];
			linePos += 2;
		}
		//Get the value of the component based off of the bits per component and the current offset.
		int res = 0;
		switch(bpc) {
			case 1:
				res = (nextInt >> (7 - offset)) & 1;
				break;
			case 2:
				res = (nextInt >> (6 - offset)) & 3;
				break;
			case 4:
				res = (nextInt >> (4 - offset)) & 15;
				break;
			case 8:
				res = nextInt;
				break;
			case 16:
				int nextByte = filter.next();
				res = (256 * (int) (nextInt & 0xFF)) + (int) (nextByte & 0xFF);
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
		int algorithm = filter.next();
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
		linePos = 0;
		nextInt = line[linePos];
		linePos ++;
		
		return line;
	}
	
	/*
	 * No prediction.
	 */
	public int[] getNoneLine() {
		int[] line = new int[byteWidth];
		for(int i = 0; i < byteWidth; i ++) {
			line[i] = filter.next();
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
				line[i] = (filter.next() + line[i - bpp]) % 256;
			}
			else {
				line[i] = filter.next() % 256;
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
				int next = filter.next();
				line[i] = (next + prevLine[i]) % 256;
			}
		}
		else {
			for(int i = 0; i < byteWidth; i ++){
				int next = filter.next();
				line[i] = next % 256;
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
			line[i] = (filter.next() + ((int) Math.floor((raw + prior)/2.0)) % 256);
		}
		return line;
	}
	
	/*
	 * Get the next line, predicting with the paeth algorithm.
	 */
	public int[] getPaethLine() {
		int[] line = new int[byteWidth];
		for(int i = 0; i < byteWidth; i ++) {
			int a = 0, b = 0, c = 0;
			if(i - bpp >= 0) {
				a = line[i - bpp];
			}
			if(prevLine != null) {
				b = prevLine[i];
				if(i - bpp >= 0) {
					c = prevLine[i - bpp];
				}
			}
			line[i] = (filter.next() + paeth(a, b, c)) % 256;
		}
		return line;
	}
	
	/*
	 * Paeth function
	 */
	public int paeth(int a, int b, int c) {
		int p = a + b - c;
		int pa = Math.abs(p - a);
		int pb = Math.abs(p - b);
		int pc = Math.abs(p - c);
		if(pa <= pb && pa <= pc) {
			return a;
		}
		else if(pb <= pa && pb <= pc) {
			return b;
		}
		return c;
	}

	@Override
	public boolean finished() {
		return filter.finished();
	}

	@Override
	public long skip(long n) {
		if(skipLine) {
			//Skip a dummy line. This fixes the problem but I have no clue why.
			nextLine();
		}
		long count = 0;
		int lines = (int) Math.floor(n / byteWidth);
		for(int i = 0; i < lines; i ++) {
			prevLine = currLine;
			currLine = nextLine();
		}
		int spaces = (int) (n % byteWidth);
		if(linePos + spaces >= byteWidth) {
			prevLine = currLine;
			currLine = nextLine();
			count += byteWidth;
			spaces -= byteWidth - linePos;
		}
		count += spaces;
		linePos += spaces;
		
		return count;
	}
	
	@Override
	public void reset() {
		try {
			filter.fis = new FileInputStream(filter.file);
			filter.fis.skip(filter.startPos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Set up the inflater and the inflater input stream. From java.util.zip.
		filter.inf = new Inflater();
		filter.iis = new InflaterInputStream(filter.fis, filter.inf);
		
		linePos = 0;
		offset = 0;
		
		prevLine = null;
		currLine = nextLine();
		nextInt = currLine[0];
		linePos ++;
		
		skipLine = true;
	}
	
	
	/*
	 * public int[] getPaethLine() 
	 */
	@Override
	public String toString() {
		return "PNGPredictor";
	}
}
