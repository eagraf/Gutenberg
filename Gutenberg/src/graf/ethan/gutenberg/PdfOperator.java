package graf.ethan.gutenberg;

public class PdfOperator {
	public String name;
	public int id;
	
	private static final String[] OPERATOR = {"b", "B", "b*", "B*", 
		"BDC", "BI", "BMC", "BT", 
		"BX", "c", "cm", "CS", 
		"cs", "d", "d0", "d1", 
		"do", "DP", "EI", "EMC",
		"ET", "EX", "f", "F", 
		"f*", "G", "g", "gs",
		"h", "i", "ID", "j", 
		"J", "K", "k", "l",
		"m", "M", "MP", "n",
		"q", "Q", "re", "RG",
		"rg", "ri", "s", "S",
		"SC", "sc", "SCN", "scn",
		"sh", "T*", "Tc", "Td",
		"TD", "Tf", "Tj", "TJ",
		"Tl", "Tm", "Tr", "Ts",
		"Tw", "Tz", "v", "w",
		"W", "W*", "y", "\'", "\""};
	
	public PdfOperator(int id) {
		this.id = id;
		this.name = OPERATOR[id];
	}
	
	@Override 
	public String toString() {
		return name;
	}
}
