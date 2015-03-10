package graf.ethan.gutenberg.core;

import graf.ethan.gutenberg.frame.BasicFrame;

import java.io.*;

/*
 * Main file for Gutenberg. Ties together the two main elements, drawer and scanner.
 */
public class GutenbergCore {
 
    public static void main(String[] args) {
    	
    	File f = new File("C:\\Users\\Ethan\\Desktop\\Gutenberg\\PDF Test\\MW - Notes - Post-Soviet Russia.pdf");
    	
    	//Initialize the scanner and drawer
    	GutenbergScanner gScanner = new GutenbergScanner(f);
    	GutenbergDrawer gDrawer = new GutenbergDrawer(gScanner);
    	gScanner.setDrawer(gDrawer);

    	//Creates the content frame
    	@SuppressWarnings("unused")
    	BasicFrame frame = new BasicFrame("Gutenberg", gDrawer);
    }
} 