package graf.ethan.gutenberg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Filter {
		public long startPos;
		public long length;
		
		private byte[] prevChunk = {};
		private byte[] chunk = {};
		private long pos;
		private int chunkCount = -1;
		
		private FileInputStream fis;
		
		boolean done = false;
		
		public Filter(long startPos, long length, File f) {
			this.startPos = startPos;
			this.length = length;
			
			this.pos = (startPos);
			
			try {
				this.fis = new FileInputStream(f);
				fis.skip(startPos);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			prevChunk = new byte[1024];
			chunk = new byte[1024];
			
			nextChunk();
		}
		
		public char nextChar() {
			pos++;
			if((pos - startPos) - (1024 * chunkCount) >= 1024) {
				System.out.println(pos);
				nextChunk();
			}
			return (char) chunk[(int) ((pos - startPos) - (1024 * chunkCount))];
		}
		
		public void back() {
			if(pos - startPos - (1024 * chunkCount) == 0) {
				chunk = prevChunk;
				chunkCount--;
			}
			pos--;	
		}
		
		public void nextChunk() {
			prevChunk = chunk;
			try {
				fis.read(chunk,  0,  1024);
			} catch (IOException e) {
				e.printStackTrace();
			}
			setChunkCount(getChunkCount() + 1);
		}
		
		public void prevChunk() {
			this.chunk = prevChunk;
			setChunkCount(getChunkCount() - 1);
		}

		public int getChunkCount() {
			return chunkCount;
		}

		public void setChunkCount(int chunkCount) {
			this.chunkCount = chunkCount;
		}

		public long getPos() {
			return pos;
		}
}
