package graf.ethan.gutenberg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Filter {
		public long startPos;
		public long length;
		
		public FileInputStream fis;
		
		public char curr;
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
		
		public char nextChar() {
			if(off == -1) {
				off = 0;
				return curr;
			}
			if(off == 0) {
				read();
				return curr;
			}
			return curr;
		}
		
		public void back() {
			off--;
		}
		
		public void read() {
			try {
				curr = (char) fis.read();
				System.out.println(curr);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
}
