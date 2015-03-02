package graf.ethan.gutenberg.filter;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;






import javax.imageio.ImageIO;


public class FilterDCT extends Filter {
	
	private BufferedImage img;
	private DataBufferByte db;
	
	private int banks;
	private int size;
	
	private int bankIndex = 0;
	private int offset = 0;
	
	public boolean finished = false;


	public FilterDCT(long startPos, long length, File f) {
		super(startPos, length, f);
		
		try {
			this.img = ImageIO.read(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.db = (DataBufferByte) img.getData().getDataBuffer();
		this.banks = db.getNumBanks();
		this.size = db.getSize();
	}
	
	@Override
	public int read() {
		if(off == -1) {
			off = 0;
			return curr;
		}
		if(off == 0) {
			if(offset >= size) {
				offset = 0;
				if(bankIndex < banks) {
					bankIndex ++;
				}
				else {
					finished = true;
				}
			}
			curr = (char) db.getElem(bankIndex, offset);
			offset ++;
			return curr;
		}
		return curr;	
	}
	
	@Override
	public boolean finished() {
		return finished;
	}
	
	@Override
	public long skip(long n) {
		long count = 0;
		bankIndex += (int) Math.floor(n / size);
		int spaces = (int) (n % size);
		if(offset + spaces >= size) {
			bankIndex ++;
			count += size;
			spaces -= size - offset;
		}
		count += spaces;
		offset += spaces;
		
		return count;
	}
	
	@Override
	public void reset() {
		try {
			fis.getChannel().position(startPos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.img = ImageIO.read(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.db = (DataBufferByte) img.getData().getDataBuffer();
		this.banks = db.getNumBanks();
		this.size = db.getSize();
	}
}
