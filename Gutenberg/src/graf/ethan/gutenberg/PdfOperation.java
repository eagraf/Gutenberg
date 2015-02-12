package graf.ethan.gutenberg;

import java.util.ArrayList;

public class PdfOperation {
	
	public PdfOperator operator;
	public ArrayList<Object> args;
	
	public PdfOperation(PdfOperator operator, ArrayList<Object> args) {
		this.operator = operator;
		this.args = args;
	}
	
	@Override
	public String toString() {
		return (operator.toString() + args.toString());
	}
}
