package graf.ethan.gutenberg.pdf;

import graf.ethan.gutenberg.core.GutenbergDrawer;
import graf.ethan.gutenberg.misc.GlyphCache;
import graf.ethan.matrix.Matrix;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;


/*
 * A class that represents the current Graphics State, i.e. the global context in which operators are used
 */
public class GraphicsState {
	public int resolution;
	public Page page;
	
	public GlyphCache cache;
	
	//The Current Transformation Matrix (CTM)
	//Maps positions from user-space to device-space
	private float scale = 1.0f;
	public Matrix ctm;
	public double ctmGraph[][];
	
	public Matrix textMatrix;
	public double tmGraph[][];
	
	public Matrix textLineMatrix;
	
	public Shape clippingPath;
	
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
	
	//Text state variables
	public float charSpace = 0;
	public float wordSpace = 0;
	public float textScale = 1;
	public float leading = 0;
	public float textRise = 0;
	public float textKnockout;
	public int renderMode = 0;
	public int fontSize;
	public String font;
		
	public GraphicsState(GutenbergDrawer drawer, Page page) {
		this.page = page;
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
		
		tmGraph = new double[3][3];
		tmGraph[0][0] = 1;
		tmGraph[1][0] = 0;
		tmGraph[2][0] = 0;
		tmGraph[0][1] = 0;
		tmGraph[1][1] = 1;
		tmGraph[2][1] = 0;
		tmGraph[0][2] = 0;
		tmGraph[1][2] = 0;
		tmGraph[2][2] = 1;
		
		this.textMatrix = new Matrix(tmGraph);
		this.textLineMatrix = textMatrix;
		
		cache = new GlyphCache();
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
	
	public void setTextStart(double a, double b, double c, double d, double e, double f) {
		tmGraph = new double[3][3];
		tmGraph[0][0] = a;
		tmGraph[1][0] = b;
		tmGraph[2][0] = 0;
		tmGraph[0][1] = c;
		tmGraph[1][1] = d;
		tmGraph[2][1] = 0;
		tmGraph[0][2] = e;
		tmGraph[1][2] = f;
		tmGraph[2][2] = 1;
		
		textMatrix = new Matrix(tmGraph);
		textLineMatrix = textMatrix;
	}
	
	public Matrix getTextRenderingMatrix() {
		double[][] trmGraph = new double[3][3];
		trmGraph[0][0] = textScale;
		trmGraph[1][0] = 0;
		trmGraph[2][0] = 0;
		trmGraph[0][1] = 0;
		trmGraph[1][1] = 1;
		trmGraph[2][1] = 0;
		trmGraph[0][2] = 0;
		trmGraph[1][2] = textRise;
		trmGraph[2][2] = 1;

		return new Matrix(trmGraph);
	}
	
	public void setClip(int x, int y, int width, int height) {
		this.clippingPath = new Rectangle(x, y, width, height);
	}
	
	public void incrementText(double width, double height) {
		tmGraph[0][2] += width;
		tmGraph[1][2] += height;
		
		textMatrix = new Matrix(tmGraph);
	}
}
