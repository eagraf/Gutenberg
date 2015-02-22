package graf.ethan.gutenberg.frame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BasicMouse extends MouseAdapter {
    
	BasicFrame frame;
	
    public BasicMouse(BasicFrame frame){
    	this.frame = frame;
    }
            			
    @Override
    public void mouseClicked(MouseEvent e){
    	System.out.println("hi");
    	if(e.getButton() == MouseEvent.BUTTON1) {
    		frame.nextPage();
    	}
    	if(e.getButton() == MouseEvent.BUTTON3) {
    		frame.prevPage();
    	}
    	if(e.getButton() == MouseEvent.BUTTON2) {
    		frame.current.y -= 50;
    		frame.current.state.ctm.set(1, 2, frame.current.state.ctm.get(1, 2) - 50);
    		frame.paint(frame.getGraphics());
    	}
    }
    
	@Override
	public void mouseDragged(MouseEvent e) {
	}
}