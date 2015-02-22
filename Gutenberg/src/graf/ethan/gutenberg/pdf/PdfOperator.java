package graf.ethan.gutenberg.pdf;

public class PdfOperator {
	public String name;
	public int id;
	
	public static final int Operator_B = 0;
	public static final int OperatorB = 1;
	public static final int Operator_B_Star = 2;
	public static final int OperatorB_Star = 3;
	public static final int OperatorBDC = 4;
	public static final int OperatorBI = 5;
	public static final int OperatorBMC = 6;
	public static final int OperatorBT = 7;
	public static final int OperatorBX = 8;
	public static final int Operator_C = 9;
	public static final int Operator_CM = 10;
	public static final int OperatorCS = 11;
	public static final int Operator_CS = 12;
	public static final int Operator_D = 13;
	public static final int Operator_D0 = 14;
	public static final int Operator_D1 = 15;
	public static final int OperatorDO = 16;
	public static final int OperatorDP = 17;
	public static final int OperatorEI = 18;
	public static final int OperatorEMC = 19;
	public static final int OperatorET = 20;
	public static final int OperatorEX = 21;
	public static final int Operator_F = 22;
	public static final int OperatorF = 23;
	public static final int Operator_F_Star = 24;
	public static final int OperatorG = 25;
	public static final int Operator_G = 26;
	public static final int Operator_GS = 27;
	public static final int Operator_H = 28;
	public static final int Operator_I = 29;
	public static final int OperatorID = 30;
	public static final int Operator_J = 31;
	public static final int OperatorJ = 32;
	public static final int OperatorK = 33;
	public static final int Operator_K = 34;
	public static final int Operator_L = 35;
	public static final int Operator_M = 36;
	public static final int OperatorM = 37;
	public static final int OperatorMP = 38;
	public static final int Operator_N = 39;
	public static final int Operator_Q = 40;
	public static final int OperatorQ = 41;
	public static final int Operator_RE = 42;
	public static final int OperatorRG = 43;
	public static final int Operator_RG = 44;
	public static final int Operator_RI = 45;
	public static final int Operator_S = 46;
	public static final int OperatorS = 47;
	public static final int OperatorSC = 48;
	public static final int Operator_SC = 49;
	public static final int OperatorSCN = 50;
	public static final int Operator_SCN = 51;
	public static final int Operator_SH = 52;
	public static final int OperatorT_Star = 53;
	public static final int Operator_TC = 54;
	public static final int Operator_TD = 55;
	public static final int OperatorTD = 56;
	public static final int Operator_TF = 57;
	public static final int Operator_TJ = 58;
	public static final int OperatorTJ = 59;
	public static final int OperatorTL = 60;
	public static final int Operator_TM = 61;
	public static final int Operator_TR = 62;
	public static final int Operator_TS = 63;
	public static final int Operator_TW = 64;
	public static final int Operator_TZ = 65;
	public static final int Operator_V = 66;
	public static final int Operator_W = 67;
	public static final int OperatorW = 68;
	public static final int OperatorW_Star = 69;
	public static final int Operator_Y = 70;
	public static final int Operator_Single = 71;
	public static final int Operator_Double = 72;
	
	private static final String[] OPERATOR = {"b", "B", "b*", "B*", 
		"BDC", "BI", "BMC", "BT", 
		"BX", "c", "cm", "CS", 
		"cs", "d", "d0", "d1", 
		"Do", "DP", "EI", "EMC",
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
