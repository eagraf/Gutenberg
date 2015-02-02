package graf.ethan.gutenberg;

import graf.ethan.matrix.Matrix;

import java.awt.geom.Point2D;

/*
 * Utility class for transforming between different coordinate spaces
 */
public class Transform {
	
	public static Point2D user_device(double x, double y, GraphicsState state) {
		double[][] graph = {{x}, {y}, {1}};
		Matrix matrix = new Matrix(graph);
		Matrix res = matrix.multiply(state.ctm);
		return new Point2D.Double(res.get(0, 0), res.get(1, 0));
	}
}
