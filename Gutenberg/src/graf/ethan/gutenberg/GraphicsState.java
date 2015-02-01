package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.util.ArrayList;


/*
 * A class that represents the current Graphics State, i.e. the global context in which operators are used
 */
public class GraphicsState {
	
	//The Current Transformation Matrix (CTM)
	//Maps positions from user-space to device-space
	Matrix ctm;
	double ctmGraph[][];
	
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
		
	public GraphicsState(GutenbergDrawer drawer, Page page) {		
		ctmGraph = new double[3][3];
		System.out.println("Scale: " + 72d/drawer.RESOLUTION);
		ctmGraph[0][0] = (drawer.RESOLUTION/72d);
		ctmGraph[1][0] = 0;
		ctmGraph[2][0] = 0;
		ctmGraph[0][1] = 0;
		ctmGraph[1][1] = (drawer.RESOLUTION/72d);
		ctmGraph[2][1] = 0;
		ctmGraph[0][2] = (double) page.x;
		ctmGraph[1][2] = (double) page.y + ((drawer.RESOLUTION/72d) * page.HEIGHT);
		ctmGraph[2][2] = 1;
		
		this.ctm = new Matrix(ctmGraph);
	}

}
