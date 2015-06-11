package graf.ethan.gutenberg.font;

import graf.ethan.gutenberg.core.GutenbergDrawer;
import graf.ethan.gutenberg.resources.Dummy;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class FontFrame extends JFrame {
	
	private static final int XOFFSET = 300;
	private static final int YOFFSET = 700;
	
	public Point[] master = new Point[0];
	public Point[] scaled = new Point[0];
	public Point[] gridFitted = new Point[0];

	public FontFrame(String title) {
		super(title);
		
		Container cont = getContentPane();
		cont.setBackground(new Color(0xFFFFFF));
		
		this.setSize(1080, 720);
    	this.setIconImage(new ImageIcon(Dummy.class.getResource("gutenberg.png")).getImage());
		this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		
		for(int i = -10; i < 30; i++) {
			g.setColor(new Color(0xBDBDBD));
			g.drawLine(100*i, 0, 100*i, 10000);
			g.drawLine(0, 100*i, 10000, 100*i);
			g.setColor(Color.BLACK);
			g.drawString(Integer.toString(-(i * 100 - YOFFSET)/2), XOFFSET - 30, i*100);
			g.drawString(Integer.toString((i * 100 - XOFFSET)/2), i*100, YOFFSET +15);

		}
		g.setColor(Color.RED);
		g.drawLine(XOFFSET, 0, XOFFSET, 10000);
		g.drawLine(0, YOFFSET, 10000, YOFFSET);
		
		g.setColor(Color.GREEN);
		for(int i = 0; i < master.length; i++) {
			g.fillOval((int) ((master[i].x)/2)+ XOFFSET, (int) (-(master[i].y+2)/2) +YOFFSET, 4, 4);
		}
		
		g.setColor(Color.BLUE);
		for(int i = 0; i < scaled.length; i++) {
			if(scaled[i].onCurve) {
				g.fillOval((int) ((scaled[i].x)*50)+ XOFFSET, (int) (-(scaled[i].y+2)*50) +YOFFSET, 4, 4);
			}
			else {
				g.drawOval((int) ((scaled[i].x)*50)+ XOFFSET, (int) (-(scaled[i].y+2)*50) +YOFFSET, 4, 4);
			}
		}
		
	}
}
