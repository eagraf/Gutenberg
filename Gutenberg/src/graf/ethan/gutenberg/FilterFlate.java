package graf.ethan.gutenberg;

import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class FilterFlate extends Filter {
	
	public InflaterInputStream iis;
	public Inflater inf;

	public FilterFlate(long startPos, long length, File f) {
		super(startPos, length, f);
		
		this.inf = new Inflater();
		this.iis = new InflaterInputStream(fis, inf);
	}
	
	@Override
	public void read() {
		try {
			curr = (char) iis.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
