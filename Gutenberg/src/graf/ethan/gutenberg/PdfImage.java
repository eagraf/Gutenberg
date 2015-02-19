package graf.ethan.gutenberg;

import java.awt.image.BufferedImage;

public class PdfImage {
	public int width;
	public int height;
	public int bitsPerComponent;
	public String colorSpace;
	
	public int byteWidth;
	public int componentNum = 0;
	
	public BufferedImage image;
	
	public PdfImage(int width, int height, int bpc, String colorSpace) {
		this.width = width;
		this.height = height;
		this.bitsPerComponent = bpc;
		this.colorSpace = colorSpace;
		
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	
		switch(colorSpace) {
			case "DeviceGray":	
				componentNum = 1;
				break;
			case "DeviceRGB":
				componentNum = 3;
				break;
			case "DeviceCMYK":
				componentNum = 4;
				break;
		}
		
		byteWidth = (int) Math.ceil(width * ((componentNum * bpc)/8));
	}

}
