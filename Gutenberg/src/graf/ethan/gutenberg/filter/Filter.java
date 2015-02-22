package graf.ethan.gutenberg.filter;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class Filter extends InputStream{
	
	public long startPos;
	public long length;
	
	public FileInputStream fis;
	
	public int curr;
	public int off = 0;
	
	public Filter(long startPos, long length, File f) {
		this.startPos = startPos;
		this.length = length;
		
		
		try {
			this.fis = new FileInputStream(f);
			fis.skip(startPos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void back() {
		off--;
	}
	
	public int read() {
		if(off == -1) {
			off = 0;
			return curr;
		}
		if(off == 0) {
			try {
				curr = (char) fis.read();
				System.out.println(curr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return curr;
		}
		return curr;	
	}
	
	public long skip(long n) {
		try {
			return fis.skip(n);
		} catch (IOException e) {
			return -1;
		}
	}
	
	public void reset() {
		off = 0;
		try {
			fis.getChannel().position(startPos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
}
