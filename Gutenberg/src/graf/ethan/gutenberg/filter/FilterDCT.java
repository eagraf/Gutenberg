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
	private int off = 0;
	
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
	public void read() {
		if(off >= size) {
			off = 0;
			if(bankIndex < banks) {
				bankIndex ++;
			}
			else {
				finished = true;
			}
		}
		curr = (char) db.getElem(bankIndex, off);
		off ++;
	}
}
