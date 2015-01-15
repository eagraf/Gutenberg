package graf.ethan.gutenberg;

import java.awt.Color;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class GutenbergCore {
 
    public static void main(String[] args) throws IOException {
    	File f = new File("C:\\Users\\Ethan\\Desktop\\PDF Test\\minimal.pdf");
    	
    	GutenbergScanner gScanner = new GutenbergScanner(f);
    	GutenbergDrawer gDrawer = new GutenbergDrawer(gScanner);

    	//Creates the content frame
    	GutenbergFrame frame = new GutenbergFrame("Gutenberg", gDrawer);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setSize(1080, 720);
    	//frame.pack();
    	frame.setIconImage(ImageIO.read(new File("C:\\Users\\Ethan\\Pictures\\GutenbergIcon.png")));
    	frame.setVisible(true);
    	
    	
    	
    	
    	
    	//gDrawer.drawPage(gScanner.getPage());
    	/*
    	FileScanner fileScanner = new FileScanner(f);
    	PdfScanner pdfScanner = new PdfScanner(fileScanner);
    	System.out.println(pdfScanner.scanObject());*/
    	
    }
} 