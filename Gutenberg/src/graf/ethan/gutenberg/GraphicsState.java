package graf.ethan.gutenberg;

import java.util.ArrayList;


/*
 * A class that represents the current Graphics State, i.e. the global context in which operators are used
 */
public class GraphicsState {
	
	//The Current Transformation Matrix (CTM)
	//Maps positions from user-space to device-space
	ArrayList<Float> ctm = new ArrayList<>();
	
	//Put Clipping Path here. Needs an implementations specific type.
	//Color
	//Line Width
	//Line Cap
	//Line Join
	//Miter Limt
	//Dash Pattern
	//Rendering Intent
	//Stroke Adjustment
	//Blend Mode
	//Soft Mask
	//Alpha Constant
	//Alpha Source
	
	//Device-dependent graphics state parameters
	//Overprint
	//Overprint Mode
	//Black Generation
	//Undercolor Removal
	//Transfer
	//Halftone
	//Flatness
	//Smoothness
	
	//Text state variables
	float charSpace = 0;
	float wordSpace = 0;
	float scale = 100;
	float leading = 0;
	float textRise = 0;
	float textKnockout;
	int renderMode = 0;
	int fontSize;
	String font;
		
	public GraphicsState() {
		
	}

}
