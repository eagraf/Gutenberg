package graf.ethan.basic;

import graf.ethan.gutenberg.GutenbergCore;
import graf.ethan.gutenberg.GutenbergDrawer;
import graf.ethan.gutenberg.Page;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/*
 * A simple frame class that extends JFrame.
 */
public class BasicFrame extends JFrame implements KeyListener{
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
		cont.setFocusable(true);
		cont.addKeyListener(this);
		
		this.setSize(1080, 720);
    	this.setIconImage(new ImageIcon(GutenbergCore.class.getResource("resources\\gutenberg.png")).getImage());
		this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
		
		this.addKeyListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawer.drawPage(g, current);
	}
	
	public void nextPage() {
		Page next =drawer.scanner.getPage(pageNum + 1);
		if(next != null) {
			current = next;
			pageNum ++;
		}
	}
	
	public void prevPage() {
		Page prev = drawer.scanner.getPage(pageNum - 1);
		if(prev != null) {
			current = prev;
			pageNum --;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println("hi");
		switch(e.getKeyCode()) {
			case KeyEvent.VK_UP:
				current.y += 10;
				break;
			case KeyEvent.VK_DOWN:
				current.y -= 10;
				break;
			case KeyEvent.VK_LEFT:
				prevPage();
				break;
			case KeyEvent.VK_RIGHT:
				nextPage();
				break;
		}
	}
}
