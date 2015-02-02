package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;


/*
 * A class that represents the current Graphics State, i.e. the global context in which operators are used
 */
public class GraphicsState {
	
	//The Current Transformation Matrix (CTM)
	//Maps positions from user-space to device-space
	public Matrix ctm;
	public double ctmGraph[][];
	
	public GeneralPath clippingPath;
	
	public Color colorStroking = Color.BLACK;
	public Color colorNonStroking = Color.BLACK;
	public ArrayList<String> colorSpace = new ArrayList<String>(Arrays.asList("DeviceGray"));
	
	public float lineWidth = 1.0f;
	public int lineCap = 0;
	public int lineJoin = 0;
	
	public float miterLimit = 10.0f;
	//Dash Pattern: Do this later, es muy raro
	
	public String renderingIntent = "RelativeColorimetric";
	public boolean strokeAdjustment = false;
	public ArrayList<String> blendMode = new ArrayList<String>(Arrays.asList("Normal"));
	//Softmask: do this later
	public float alphaConstant = 1.0f;
	public boolean alphSource = false;
	
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
	public float charSpace = 0;
	public float wordSpace = 0;
	public float scale = 100;
	public float leading = 0;
	public float textRise = 0;
	public float textKnockout;
	public int renderMode = 0;
	public int fontSize;
	public String font;
		
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
