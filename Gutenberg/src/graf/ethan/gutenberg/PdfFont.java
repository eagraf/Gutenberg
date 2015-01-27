package graf.ethan.gutenberg;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

/*
 * Represents a font object with incomplete information, allows for the actual font object to be created when
 * information is present.
 */
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
	
	/*
	 * Returns a font object.
	 */
	public Font getFont(int style, int size) {
		Font font = new Font(fileName, style, size);
		try {
			font = Font.createFont(fontFormat, fontFile);
			font = font.deriveFont(style, size);
		} catch (FontFormatException e) {
			System.out.println("hi");
			e.printStackTrace();
		} catch (IOException e) {
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
