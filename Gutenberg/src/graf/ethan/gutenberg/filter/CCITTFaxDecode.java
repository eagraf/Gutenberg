package graf.ethan.gutenberg.filter;

import graf.ethan.gutenberg.pdf.PdfDictionary;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CCITTFaxDecode extends Filterless{
	
	private PdfDictionary params;
	
	private CCITTFaxDecoder decoder;
	
	public int columns;
	public int rows;
	
	private BufferedImage img;
	
	private int x;
	private int y;
	
	public CCITTFaxDecode(long startPos, long length, PdfDictionary parms, File f, int width, int height) {
		super(startPos, length, f);
		this.params = parms;
		
		this.columns = width;
		this.rows = height;
		
		try {
			this.decoder = new CCITTFaxDecoder(this, rows, columns);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			decoder.decodeT62D();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.img = decoder.getImage();
	}

	@Override
	public int read() {
		int res = img.getRGB(x, y);
		if(x == columns-1) {
			x = 0;
			y ++;
		}
		else {
			x ++;
		}
		return res;
	}

	@Override
	//This has to be fixed
	public long skip(long n) {
		long count = 0;
		for(int i = 0; i < n; i ++) {
			read();
			count ++;
		}
		return count;
	}

	@Override
	public void reset() {
		//This probably needs to be fixed.
		x = 0;
		y = 0;
	}

	@Override
	public boolean finished() {
		if(x == columns - 1 && y == rows -1) {
			return true;
		}
		return false;
	}
	
	public int nextByte() {
		return super.read();
	}
}
