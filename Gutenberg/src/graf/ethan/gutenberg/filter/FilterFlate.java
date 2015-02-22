package graf.ethan.gutenberg.filter;


import graf.ethan.gutenberg.pdf.PdfDictionary;

import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


public class FilterFlate extends Filter {
	
	public InflaterInputStream iis;
	public Inflater inf;
	
	private int predictor = 1;
	private int colors = 1;
	private int bpc = 8;
	private int columns = 1;
	
	private int linePos = 0;
	private int offset = 0;
	private int byteWidth;
	private int bpp;
	private int[] currLine;
	private int[] prevLine;
	
	/*
	 * A filter based on the inflate/deflate algorithm for zlib compression.
	 * Can take parameters for better compression.
	 *  * THIS NEEDS TO BE FIXED *
	 */
	public FilterFlate(long startPos, long length, PdfDictionary params, File f) {
		super(startPos, length, f);
		
		//Set up the inflater and the inflater input stream. From java.util.zip.
		this.inf = new Inflater();
		this.iis = new InflaterInputStream(fis, inf);
		
		/*
		 * Get parameters. For FlateDecode parameters usually are not necesarry.
		 * All parameters relate to predictor algorithms.
		 */
		if(params != null) {
			if(params.has("Predictor")) {
				this.predictor = (int) params.get("Predictor");
			}
			if(predictor > 1) {
				if(params.has("Colors")) {
					this.colors = (int) params.get("Colors");
				}
				if(params.has("BitsPerComponent")) {
					this.bpc = (int) params.get("BitsPerComponent");
				}
				if(params.has("Columns")) {
					this.columns = (int) params.get("Columns");
				}
				
				bpp = (int) Math.ceil((bpc * colors)/8.0);
				byteWidth = (int) Math.ceil(bpp * columns);
				
				currLine = nextLine();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see graf.ethan.gutenberg.Filter#read()
	 * Discontinuity between read method and nextComponent method. 
	 * This discontinuity is the principle problem with this class.
	 */
	@Override
	public void read() {
		try {
			curr = (char) iis.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				int nextByte = nextChar();
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
		//If the predictor is within the PNG specification.
		if(predictor == 10 || predictor == 11 || predictor == 12 || predictor == 13 || predictor == 14 || predictor == 15) {
			int algorithm = nextChar();
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
		}
		return line;
	}
	
	/*
	 * No prediction.
	 */
	public int[] getNoneLine() {
		int[] line = new int[byteWidth];
		for(int i = 0; i < byteWidth; i ++) {
			line[i] = nextChar();
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
				line[i] = (nextChar() + line[i - bpp]) % 256;
			}
			else {
				line[i] = nextChar() % 256;
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
				line[i] = (nextChar() + prevLine[i]) % 256;
			}
		}
		else {
			for(int i = 0; i < byteWidth; i ++){		
				line[i] = nextChar() % 256;
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
			line[i] = ((int) Math.floor((raw + prior)/2.0)) % 256;
		}
		return line;
	}
	
	/*
	 * public int[] getPaethLine() 
	 */
	
	/*
	 * If a predictor algorithm is being used, determine whether the stream is finished.
	 */
	public boolean finishedPredictor() {
		if(inf.finished() && linePos == byteWidth) {
			return true;
		}
		return false;
	}
}