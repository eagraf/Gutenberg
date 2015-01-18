package graf.ethan.gutenberg;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

public class PdfFont {
	private String fileName;
	private int fontFormat;
	private File fontFile;
	
	public PdfFont(String fileName, int fontFormat, File fontFile) {
		this.fileName = fileName;
		this.fontFormat = fontFormat;
		this.fontFile = fontFile;
		System.out.println(fontFile);
	}
	
	public Font getFont(int style, int size) {
		Font font = new Font(fileName, style, size);
		try {
			font = Font.createFont(fontFormat, fontFile);
			font = font.deriveFont(style, size);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			System.out.println("hi");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("hi");
			e.printStackTrace();
		}
		GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
		return font;
	}
	
	@Override
	public String toString() {
		return fileName;
	}
}
