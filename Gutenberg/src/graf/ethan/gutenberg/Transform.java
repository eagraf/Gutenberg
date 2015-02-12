package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.geom.Point2D;

/*
 * Utility class for transforming between different coordinate spaces
 */
public class Transform {
	
	public float scale = 1.0f;
	
	public static Point2D user_device(double x, double y, GraphicsState state) {
		double[][] graph = {{x}, {y}, {1}};
		Matrix matrix = new Matrix(graph);
		Matrix res = matrix.multiply(state.ctm);
		return new Point2D.Double(res.get(0, 0), res.get(1, 0));
	}
	
	public static int scale(int num, GraphicsState state) {
		return (int) (num * state.ctm.get(0, 0));
	}
	
	public static long scale(long num, GraphicsState state) {
		return (long) (num * state.ctm.get(0, 0));
	}
	
	public static float scale(float num, GraphicsState state) {
		return (float) (num * state.ctm.get(0, 0));
	}
	
	public static double scale(double num, GraphicsState state) {
		return (double) (num * state.ctm.get(0, 0));
	}
}
