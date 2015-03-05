package graf.ethan.gutenberg.filter;


import graf.ethan.gutenberg.pdf.PdfDictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


public class FlateDecode extends PredictorFilter{
	
	/*
	 * A filter based on the inflate/deflate algorithm for zlib compression.
	 * Can take parameters for better compression.
	 *  * THIS NEEDS TO BE FIXED *
	 */
	public FlateDecode(long startPos, long length, PdfDictionary parms, File f) {
		super(startPos, length, f);
		
		this.params = parms;
		
		//Set up the inflater and the inflater input stream. From java.util.zip.
		this.inf = new Inflater();
		this.iis = new InflaterInputStream(fis, inf);
		
		/*
		 * Get parameters. For FlateDecode parameters usually are not necesarry.
		 * All parameters relate to predictor algorithms.
		 */
		if(params != null) {
			getPredictor();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see graf.ethan.gutenberg.Filter#read()
	 * Discontinuity between read method and nextComponent method. 
	 * This discontinuity is the principle problem with this class.
	 */
	@Override
	public int read() {
		if(PNGPredictor || TIFFPredictor) {
			return predictor.nextComponent();
		}
		if(off == -1) {
			off = 0;
			return curr;
		}
		if(off == 0) {
			try {
				curr = (char) iis.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return curr;
		}
		return curr;	
	}
	
	public int next() {
		try {
			curr = (char) iis.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return curr;
	}
	
	public long skip(long n) {
		if(PNGPredictor || TIFFPredictor) {
			return predictor.skip(n);
		}
		try {
			return iis.skip(n);
		} catch (IOException e) {
			return -1;
		}
	}
	
	public void close() {
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		inf.end();
	}
	
	public boolean finished() {
		return inf.finished();
	}
	
	@Override
	public void reset() {
		if(PNGPredictor || TIFFPredictor) {
			predictor.reset();
		}
		try {
			fis = new FileInputStream(file);
			fis.skip(startPos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Set up the inflater and the inflater input stream. From java.util.zip.
		this.inf = new Inflater();
		this.iis = new InflaterInputStream(fis, inf);
	}
	
	/*
	 * Set the predictor if one is specified.
	 */
	public void getPredictor() {
		if(params.has("Predictor")) {
			this.predictorNum = (int) params.get("Predictor");
		}
		if(predictorNum > 1) {
			int colors = 1;
			int bpc = 8;
			int columns = 1;
			if(params.has("Colors")) {
				colors = (int) params.get("Colors");
			}
			if(params.has("BitsPerComponent")) {
				bpc = (int) params.get("BitsPerComponent");
			}
			if(params.has("Columns")) {
				columns = (int) params.get("Columns");
			}
			switch(predictorNum) {
				case 2:
					TIFFPredictor = true;
					this.predictor = new PredictorTIFF(this, colors, bpc, columns);
					break;
				case 10:
				case 11:
				case 12:
				case 13:
				case 14:
				case 15:
					PNGPredictor = true;
					
					this.predictor = new PredictorPNG(this, colors, bpc, columns);
					System.out.println(predictor.toString());
					break;
			}
		}
	}
}