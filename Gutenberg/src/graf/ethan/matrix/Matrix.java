package graf.ethan.matrix;

/*
 * Represents algebraic matrices
 */
public class Matrix {
	
	public int rows;
	public int columns;
	
	//2 Dimensional array represents the matrix ([x][y]).
	public double graph[][];
	
	public Matrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		
		this.graph = new double[columns][rows];
		for(int x = 0; x < columns; x ++) {
			for(int y = 0; y < rows; y ++) {
				graph[x][y] = 0;
			}
		}
	}
	
	public Matrix(int rows, int columns, double value) {
		this.rows = rows;
		this.columns = columns;
		
		this.graph = new double[columns][rows];
		for(int x = 0; x < columns; x ++) {
			for(int y = 0; y < rows; y ++) {
				graph[x][y] = value;
			}
		}
	}
	
	public Matrix(double graph[][]) {
		this.graph = graph;
		
		this.rows = graph[0].length;
		this.columns = graph.length;
	}
	
	public void set(int x, int y, double value) {
		graph[x][y] = value;
	}
	
	public void setGraph(double[][] graph) {
		this.graph = graph;
	}
	
	
	public void setGraph(double value) {
		for(int x = 0; x < columns; x ++) {
			for(int y = 0; y < rows; y ++) {
				graph[x][y] = value;
			}
		}
	}
	
	public double get(int x, int y) {
		return graph[x][y];
	}
	
	public double[][] getGraph() {
		return graph;
	}
	
	//The transpose of the matrix (x and y are switched).
	public Matrix transpose() {
        double newGraph[][] = new double[rows][columns];
        for(int x = 0; x < columns; x ++) {
        	for(int y = 0; y < rows; y ++) {
        		newGraph[y][x] = graph[x][y];
        	}
        }
        return new Matrix(newGraph);
	}
	
	//Matrix addition: Corresponding elements are added to eachother.
	public Matrix add(Matrix matrix) {
		if(matrix.columns != columns || matrix.rows != rows) {
			throw new IllegalArgumentException("Matrix dimensions must be identical");
		}
		double[][] newGraph = new double[columns][rows];
		for(int x = 0; x < columns; x ++) {
			for(int y = 0; y < rows; y ++) {
				newGraph[x][y] = graph[x][y] + matrix.graph[x][y];
			}
		}
		return new Matrix(newGraph);
	}
	
	public static Matrix add(Matrix matrix1, Matrix matrix2) {
		if(matrix2.columns != matrix1.columns || matrix2.rows != matrix1.rows) {
			throw new IllegalArgumentException("Matrix dimensions must be identical");
		}
		double[][] newGraph = new double[matrix1.columns][matrix1.rows];
		for(int x = 0; x < matrix1.columns; x ++) {
			for(int y = 0; y < matrix1.rows; y ++) {
				newGraph[x][y] = matrix1.graph[x][y] + matrix2.graph[x][y];
			}
		}
		return new Matrix(newGraph);
	}
	
	public Matrix multiply(double scalar) {
		for(int x = 0; x < columns; x ++) {
			for(int y = 0; y < rows; y ++) {
				graph[x][y] = graph[x][y] * scalar;
			}
		}
		return this;
	}
	
	//Matrix multiplacation: It aint like regular multiplacation.
	public Matrix multiply(Matrix matrix) {
		if(columns != matrix.rows) {
			throw new IllegalArgumentException("Number of columns in Matrix 1 must equal the number of rows in Matrix 2");
		}
		double[][] newGraph = new double[matrix.columns][rows];
		for(int x = 0; x < matrix.columns; x ++) {
			for(int y = 0; y < rows; y ++) {
				double value = 0;
				for(int i = 0; i < columns; i ++) {
					value += graph[i][y] * matrix.graph[x][i];
				}
				newGraph[x][y] = value;
			}
		}
		return new Matrix(newGraph);
	}
	
	public static Matrix multiply(Matrix matrix1, Matrix matrix2) {
		if(matrix1.columns != matrix2.rows) {
			throw new IllegalArgumentException("Number of columns int Matrix 1 must equal the number of rows in Matrix 2");
		}
		double[][] newGraph = new double[matrix2.columns][matrix1.rows];
		for(int x = 0; x < matrix2.columns; x ++) {
			for(int y = 0; y < matrix1.rows; y ++) {
				double value = 0;
				for(int i = 0; i < matrix1.columns; i ++) {
					value += matrix1.graph[i][y] * matrix2.graph[x][i];
				}
				newGraph[x][y] = value;
			}
		}
		return new Matrix(newGraph);
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for(int y = 0; y < rows; y ++) {
			res.append('[');
			for(int x = 0; x < columns; x ++) {
				res.append(' ');
				res.append(graph[x][y]);
			}
			res.append(']');
			res.append('\n');
		}
		return res.toString();
	}

}
