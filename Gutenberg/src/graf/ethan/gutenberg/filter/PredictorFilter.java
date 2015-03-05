package graf.ethan.gutenberg.filter;

import graf.ethan.gutenberg.pdf.PdfDictionary;

import java.io.File;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public abstract class PredictorFilter extends Filterless {
	
	public InflaterInputStream iis;
	public Inflater inf;
	
	public boolean PNGPredictor = false;
	public boolean TIFFPredictor = false;
	
	public Predictor predictor;
	
	public PdfDictionary params;
	
	public int predictorNum = 1;
	
	public PredictorFilter(long startPos, long length, File f) {
		super(startPos, length, f);
	}
	
	public abstract int next();
}
