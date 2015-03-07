package graf.ethan.gutenberg.filter;

import java.io.File;

public class RunLengthDecode extends Filterless {
	
	public int[] buffer;
	public int offset = 0;
	public int dup = 0;
	
	public boolean finished = false;

	public RunLengthDecode(long startPos, long length, File f) {
		super(startPos, length, f);
	}
	
	@Override
	public int read() {
		if(offset == dup) {
			dup = super.read();
			if((dup & 0xFF) != 128) {
				if(dup >= 0 && dup <= 127) {
					buffer = new int[dup + 1];
					for(int i = 0; i < dup + 1; i ++) {
						buffer[i] = super.read();
					}
				}
				else {
					int dupByte = super.read();
					buffer = new int[257 - dup];
					for(int i = 0; i < 257 - dup; i ++) {
						buffer[i] = dupByte;
					}
				}
			}
			else {
				this.finished = true;
			}
		}
		int res = buffer[offset];
		offset ++;
		return res;
	}
	
	@Override 
	public long skip(long n) {
		for(long i = 0; i < n; i ++) {
			read();
			if(finished) {
				return i;
			}
		}
		return n;
	}
	
	@Override
	public void reset() {
		offset = 0;
		dup = 0;
		super.reset();
	}
	
	@Override
	public boolean finished() {
		return finished;
	}
}
