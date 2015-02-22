package graf.ethan.gutenberg.scanner;


import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.filter.Filter;
import graf.ethan.gutenberg.filter.FilterDCT;
import graf.ethan.gutenberg.filter.FilterFlate;
import graf.ethan.gutenberg.pdf.PdfDictionary;
import graf.ethan.gutenberg.pdf.PdfImage;
import graf.ethan.gutenberg.pdf.PdfObjectReference;
import graf.ethan.gutenberg.pdf.PdfXObject;

import java.awt.Color;


public class XObjectScanner {
	GutenbergScanner scanner;
	
	public long startPos;
	public long length;
	
	public PdfDictionary streamDictionary;
	
	public Filter filter;
	
	int componentCycle = 0;
	int currentByte;
	
	public XObjectScanner(GutenbergScanner scanner) {
		this.scanner = scanner;
	}
	
	public PdfXObject scanObject(PdfObjectReference reference) {
		scanner.fileScanner.setPosition(scanner.crossScanner.getObjectPosition(reference));
		
		//Scan in the stream dictionary
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		scanner.pdfScanner.scanNumeric();
		scanner.pdfScanner.skipWhiteSpace();
		
		if(scanner.pdfScanner.scanKeyword() == 2) {
			streamDictionary = (PdfDictionary) scanner.pdfScanner.scanNext();
			length = ((Number) streamDictionary.get("Length")).longValue();
			
			//Begin the scanning process
			scanner.pdfScanner.scanKeyword();
			scanner.pdfScanner.skipWhiteSpace();
			startPos = scanner.fileScanner.getPosition();
			
			PdfDictionary params = null;
			if(streamDictionary.has("DecodeParms")) {
				params = (PdfDictionary) streamDictionary.get("DecodeParms");
			}
			
			if(streamDictionary.has("Filter")) {
				String filterName = (String) streamDictionary.get("Filter");
				switch(filterName) {
					case "FlateDecode":
						filter = new FilterFlate(startPos, length, params, scanner.fileScanner.file);
						break;
					case "DCTDecode":
						filter = new FilterDCT(startPos, length, scanner.fileScanner.file);
						break;
				}
				System.out.println("Stream Dictionary: " + streamDictionary);
			}
			else {
				filter = new Filter(startPos, length, scanner.fileScanner.file);
			}
			
			if(streamDictionary.has("Subtype")) {
				String subType = (String) streamDictionary.get("Subtype");
				//This is incomplete
				switch(subType) {
					case("Image"):
						return new PdfXObject(streamDictionary, scanImage());
					case("Form"):
						break;
				}
			}	
		}
		return null;
	}
	
	public PdfImage scanImage() {
		//The four variables for specifying an image stream
		int bpc = ((Number) streamDictionary.get("BitsPerComponent")).intValue();
		int width = ((Number) streamDictionary.get("Width")).intValue();
		int height = ((Number) streamDictionary.get("Height")).intValue();
		String colorSpace = (String) streamDictionary.get("ColorSpace");
		
		PdfImage image = new PdfImage(width, height, bpc, colorSpace);
		
		switch(colorSpace) {
			case "DeviceRGB":
				for(int y = 0; y < height; y ++) {
					for(int x = 0; x < width; x ++) {
						int[] components = new int[3];
						for(int i = 0; i < 3; i ++) {
							components[i] = nextComponent(image);
						}
						int rgb = new Color(components[2], components[1], components[0]).getRGB();
						image.image.setRGB(x, y, rgb);
					}
				}
				break;
			case "DeviceGray":
				for(int y = 0; y < height; y ++) {
					for(int x = 0; x < width; x ++) {
						int component = nextComponent(image);
						int rgb = new Color(component, component, component).getRGB();
						image.image.setRGB(x, y, rgb);
					}
				}
				break;
		}
		
		System.out.println(image);
		return image;
	}
	
	public int nextComponent(PdfImage image) {
		if(componentCycle == image.bitsPerComponent) {
			componentCycle = 0;
			currentByte = filter.read();
		}
		int next;
		switch(image.bitsPerComponent) {
			case 1:
				next = (currentByte >> (7 - componentCycle)) & 1;
				break;
			case 2:
				next = (currentByte >> (6 - componentCycle)) & 3;
				break;
			case 4:
				next = (currentByte >> (4 - componentCycle)) & 15;
				break;
			case 8:
				next = currentByte;
				break;
			case 16:
				byte nextByte = (byte) filter.read();
				next = (256 * (int) (currentByte & 0xFF)) + (int) (nextByte & 0xFF);
				break;
			default:
				next = 0;
				break;
		}
		componentCycle += image.bitsPerComponent;
		return next;
	}
}
