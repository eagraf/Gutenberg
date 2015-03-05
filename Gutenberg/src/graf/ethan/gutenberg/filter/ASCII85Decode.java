package graf.ethan.gutenberg.filter;

import java.io.File;
import java.io.IOException;

public class ASCII85Decode extends Filterless {
	
	public int raw[] = new int[4];
	public int decoded[] = new int[5];
	public int offset = 5;
	
	public boolean finished = false;

	public ASCII85Decode(long startPos, long length, File f) {
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
			if(offset == 5) {
				nextChunk();
				offset = 0;
			}
			curr = decoded[offset];
			return curr;
		}
		return curr;	
	}
	
	public void nextChunk() {
		for(int i = 0; i < 4; i ++) {
			try {
				int next = fis.read();
				if((char) next == '~') {
					if((char) fis.read() == '>') {
						finished = true;
					}
				}
				if(next >= 33 && next < 118) {
					raw[i] = next - 33;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long num = (long) ((raw[1] * Math.pow(256, 3))
				+ (raw[2] * Math.pow(256, 2))
				+ (raw[1] * 256)
				+ raw[0]);
		for(int i = 0; i < 5; i ++) {
			decoded[i] =  ((int) num / (int) Math.pow(85, 5-i-1));
		}
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
