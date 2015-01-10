package graf.ethan.gutenberg;

import java.io.*;

import javax.swing.JFrame;

public class GutenbergCore {
 
    public static void main(String[] args) throws IOException {
    	//Creates the content frame
    	JFrame frame = new JFrame("Gutenberg");
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.pack();
    	frame.setVisible(true);
    	
    	File f = new File("C:\\Users\\Ethan\\Desktop\\PDF Test\\minimal.pdf");
    	
    	FileScanner scanner = new FileScanner(f);
    	PdfScanner pdfScanner = new PdfScanner(scanner);
    	GutenbergScanner gScanner = new GutenbergScanner(f);
    	
    	gScanner.firstPass();
    	//System.out.println(pdfScanner.scanNext().toString());
    	
    }
} 