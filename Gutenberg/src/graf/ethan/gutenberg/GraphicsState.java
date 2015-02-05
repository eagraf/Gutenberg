package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;


/*
 * A class that represents the current Graphics State, i.e. the global context in which operators are used
 */
public class GraphicsState {
	public int resolution;
	public static Page page;
	
	//The Current Transformation Matrix (CTM)
	//Maps positions from user-space to device-space
	private float scale = 1.0f;
	public Matrix ctm;
	public double ctmGraph[][];
	
	public GeneralPath clippingPath;
	
	public String colorSpaceStroking = "DeviceGray";
	public String colorSpaceNonStroking = "DeviceGray";
	public Color colorStroking = Color.BLACK;
	public Color colorNonStroking = Color.BLACK;
	
	//Stroke Parameters
	public float lineWidth = 1.0f;
	public int lineCap = 0;
	public int lineJoin = 0;
	public float miterLimit = 10.0f;
	public ArrayList<Number> dashArray = new ArrayList<Number>(Arrays.asList(0));
	public float phase = 0;
	
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
	public float textScale = 100;
	public float leading = 0;
	public float textRise = 0;
	public float textKnockout;
	public int renderMode = 0;
	public int fontSize;
	public String font;
		
	public GraphicsState(GutenbergDrawer drawer, Page page) {		
		this.resolution = drawer.RESOLUTION;
		
		ctmGraph = new double[3][3];
		ctmGraph[0][0] = (scale * resolution/72d);
		ctmGraph[1][0] = 0;
		ctmGraph[2][0] = 0;
		ctmGraph[0][1] = 0;
		ctmGraph[1][1] = (scale * -resolution/72d);
		ctmGraph[2][1] = 0;
		ctmGraph[0][2] = (double) page.x;
		ctmGraph[1][2] = (double) ((scale * resolution/72d) * page.HEIGHT) +  page.y;
		ctmGraph[2][2] = 1;
		
		this.ctm = new Matrix(ctmGraph);
	}
	
	public void setScale(float scale) {
		ctmGraph = new double[3][3];
		ctmGraph[0][0] = (scale * resolution/72d);
		ctmGraph[1][0] = 0;
		ctmGraph[2][0] = 0;
		ctmGraph[0][1] = 0;
		ctmGraph[1][1] = (scale * resolution/72d);
		ctmGraph[2][1] = 0;
		ctmGraph[0][2] = (double) page.x;
		ctmGraph[1][2] = (double) ((scale * resolution/72d) * page.HEIGHT) +  page.y;
		ctmGraph[2][2] = 1;
		
		this.ctm = new Matrix(ctmGraph);
	}
}
