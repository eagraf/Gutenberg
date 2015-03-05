package graf.ethan.gutenberg.filter;

import java.io.File;
import java.io.IOException;

public class ASCIIHexDecode extends Filterless {

	public ASCIIHexDecode(long startPos, long length, File f) {
		super(startPos, length, f);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int read() {
		if(off == -1) {
			off = 0;
			return curr;
		}
		if(off == 0) {
			StringBuilder sb = new StringBuilder();
			try {
				sb.append(fis.read());
				sb.append(fis.read());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return curr;
		}
		return curr;	
	}
	
	@Override
	public long skip(long n) {
		try {
			return fis.skip(n * 2);
		} catch (IOException e) {
			return 0;
		}
	}
	
	@Override
	public void close() {
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	@Override
	public boolean finished() {
		try {
			if(fis.getChannel().position() > startPos + length) {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
		return false;
	}
	
	@Override
	public void reset() {
		off = 0;
		try {
			fis.getChannel().position(startPos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
