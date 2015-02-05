package graf.ethan.gutenberg;

import java.awt.Color;
import java.io.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/*
 * Main file for Gutenberg. Ties together the two main elements, drawer and scanner.
 */
public class GutenbergCore {
 
    public static void main(String[] args) throws IOException {
    	//Path to a test PDF file
    	File f = new File("C:\\Users\\Ethan\\Desktop\\Gutenberg\\PDF Test\\minimal.pdf");
    	
    	//Initialize the scanner and drawer
    	GutenbergScanner gScanner = new GutenbergScanner(f);
    	GutenbergDrawer gDrawer = new GutenbergDrawer(gScanner);
    	gScanner.setDrawer(gDrawer);

    	//Creates the content frame
    	GutenbergFrame frame = new GutenbergFrame("Gutenberg", gDrawer);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setSize(1080, 720);
    	frame.setIconImage(new ImageIcon(GutenbergCore.class.getResource("resources\\gutenberg.png")).getImage());
    	frame.getContentPane().setBackground(new Color(0xBDBDBD));
    	frame.setVisible(true);
    }
} 