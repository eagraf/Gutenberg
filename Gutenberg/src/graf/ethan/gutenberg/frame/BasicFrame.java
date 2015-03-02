package graf.ethan.gutenberg.frame;

import graf.ethan.gutenberg.core.GutenbergDrawer;
import graf.ethan.gutenberg.pdf.Page;
import graf.ethan.gutenberg.resources.Dummy;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/*
 * A simple frame class that extends JFrame.
 */
public class BasicFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private GutenbergDrawer drawer;
	public Page current;
	private int pageNum = 0;
	
	public BasicFrame(String title, GutenbergDrawer drawer) {
		super(title);
		this.drawer = drawer;
		current = drawer.scanner.getPage(pageNum);
		
		Container cont = getContentPane();
		cont.setBackground(new Color(0xBDBDBD));
		
		this.setSize(1080, 720);
    	this.setIconImage(new ImageIcon(Dummy.class.getResource("gutenberg.png")).getImage());
		this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
		
		this.addKeyListener(new BasicKey(this));
		BasicMouse m = new BasicMouse(this);
        addMouseListener(m);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawer.drawPage(g, current);
	}
	
	public void nextPage() {
		Page next = drawer.scanner.getPage(pageNum + 1);
		if(next != null) {
			current = next;
			pageNum ++;
			paint(getGraphics());
		}
	}
	
	public void prevPage() {
		Page prev = drawer.scanner.getPage(pageNum - 1);
		if(prev != null) {
			current = prev;
			pageNum --;
			paint(getGraphics());
		}
	}
}
