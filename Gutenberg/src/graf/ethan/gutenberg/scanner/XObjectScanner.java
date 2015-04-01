package graf.ethan.gutenberg.scanner;


import graf.ethan.gutenberg.core.GutenbergScanner;
import graf.ethan.gutenberg.filter.CCITTFaxDecode;
import graf.ethan.gutenberg.filter.Filterless;
import graf.ethan.gutenberg.filter.DCTDecode;
import graf.ethan.gutenberg.filter.FlateDecode;
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
	
	public Filterless filter;
	
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
						filter = new FlateDecode(startPos, length, params, scanner.fileScanner.file);
						break;
					case "DCTDecode":
						filter = new DCTDecode(startPos, length, scanner.fileScanner.file);
						break;
					case "CCITTFaxDecode":
						filter = new CCITTFaxDecode(startPos, length, params, scanner.fileScanner.file, ((Number) streamDictionary.get("Width")).intValue(), ((Number) streamDictionary.get("Height")).intValue());
						break;
				}
				System.out.println("Stream Dictionary: " + streamDictionary);
			}
			else {
				filter = new Filterless(startPos, length, scanner.fileScanner.file);
			}
			System.out.println(filter);
			
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
		
		if(colorSpace != null) {
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
		}
		else {
			for(int y = 0; y < height; y ++) {
				for(int x = 0; x < width; x ++) {
					int component = nextComponent(image);
					int rgb = new Color(component, component, component).getRGB();
					image.image.setRGB(x, y, rgb);
				}
			}
		}
		
		return image;
	}
	
	public int nextComponent(PdfImage image) {
		if(filter.getClass() == CCITTFaxDecode.class) {
			int res = filter.read();
			if(res == 1) {
				return 255;
			}
			return 0;
		}
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
