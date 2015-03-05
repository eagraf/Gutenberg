package graf.ethan.gutenberg.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/*
 * Predictor Function 2 from the TIFF Standard.
 * I couldn't find good documentation on this, so this might not wor :(.
 */
public class PredictorTIFF extends Predictor {
	
	private PredictorFilter filter;
	
	private int colors;
	private int bpc;
	private int columns;
	private int byteWidth;
	private int bpp;
	
	private int linePos = 0;
	private int offset = 0;
	private int width;
	
	private int[] currLine;

	private boolean skipLine;
	
	public PredictorTIFF(PredictorFilter filter, int colors, int bpc, int columns) {
		this.filter = filter;
		
		this.colors = colors;
		this.bpc = bpc;
		this.columns = columns;
		
		bpp = (int) Math.ceil((this.bpc * this.colors)/8.0);
		byteWidth = (int) Math.ceil(bpp * this.columns);
		
		width = this.colors * this.columns;
		
		currLine = nextLine();
	}

	@Override
	public int nextComponent() {
		if(linePos == width) {
			currLine = nextLine();
			offset = 0;
			linePos = 0;
		}
		linePos ++;
		return currLine[linePos - 1];
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
			currLine = nextLine();
			count += byteWidth;
		}
		int spaces = (int) ((n % byteWidth) / (this.bpc / 8.0));
		if(linePos + spaces >= width) {
			currLine = nextLine();
			count += byteWidth;
			spaces -= width - linePos;
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
		
		currLine = nextLine();
		
		skipLine = true;
	}
	
	public int[] nextLine() {
		int[] line = new int[width];
		int currByte = filter.read();
		for(int i = 0; i < width; i ++) {
			if(offset == 8) {
				offset = 0;
				currByte = filter.read();
			}
			if(offset == 16) {
				offset = 0;
				currByte = filter.read();
			}
			int next = 0;
			switch(bpc) {
				case 1:
					next = (currByte >> (7 - offset)) & 1;
					break;
				case 2:
					next = (currByte >> (6 - offset)) & 3;
					break;
				case 4:
					next = (currByte >> (4 - offset)) & 15;
					break;
				case 8:
					next = currByte;
					break;
				case 16:
					int nextByte = filter.read();
					next = (256 * (int) (currByte & 0xFF)) + (int) (nextByte & 0xFF);
					break;
			}
			offset += bpc;
			if(i - colors >= 0) {
				line[i] = next + line[i - colors];
			}
			else {
				line[i] = next;
			}
		}
		return line;
	}
}
