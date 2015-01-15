package graf.ethan.gutenberg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class GutenbergDrawer {
	public GutenbergScanner scanner;
	
	public GutenbergDrawer(GutenbergScanner scanner) {
		this.scanner = scanner;
	}
	
	public void drawPage(Graphics g, Page page) {
		g.setColor(Color.WHITE);
		g.fillRect(200, 40, page.WIDTH, page.HEIGHT);
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 240));
		g.drawString("Hello World", 300, 100);
	}

}
