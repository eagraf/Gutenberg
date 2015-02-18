package graf.ethan.basic;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class BasicKey extends KeyAdapter {
	BasicFrame frame;
	
	public BasicKey(BasicFrame frame) {
		this.frame = frame;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println("hi");
		switch(e.getKeyCode()) {
			case KeyEvent.VK_UP:
				frame.current.y += 10;
				break;
			case KeyEvent.VK_DOWN:
				frame.current.y -= 10;
				break;
			case KeyEvent.VK_LEFT:
				frame.prevPage();
				break;
			case KeyEvent.VK_RIGHT:
				frame.nextPage();
				break;
		}
	}
}
