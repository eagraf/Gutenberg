package graf.ethan.gutenberg.font;

import java.util.Stack;

/*
 * The graphics state for a TrueType font. Begins with default values, does not transfer between glyphs.
 */
public class GraphicsState {

	public Stack<StackElement> stack;
	
	int instructionPointer = 0;
	
	public boolean condition;
	public int depth = 0;
	
	//Zone Pointers
	public int zp0 = 1;
	public int zp1 = 1;
	public int zp2 = 1;
	
	//Zones
	public PointF26Dot6[][] zone;
	
	public PointF26Dot6[][] original;
	
	//Reference Points
	public int rp[] = new int[3];
	
	//Round State Variables (26.6 Numbers).
	public boolean roundingOn = true;
	public int period = 0x00000040;
	public int phase = 0;
	public int threshold = 0x00000020;
	
	//Freedom Vector
	public PointF2Dot14 fv = new PointF2Dot14((short) 0xCFFF, (short) 0);

	//Projection Vector
	public PointF2Dot14 pv = new PointF2Dot14((short) 0xCFFF, (short) 0);
	
	//Dual Projection Vector
	public PointF2Dot14 dpv = new PointF2Dot14((short) 0xCFFF, (short) 0);
	
	//Control Value Table Cut In
	public int cutIn = 0x00000044;
	
	//Single Width
	public int singleWidth = 0;
	public int singleWidthCutIn = 0;
	
	//Minimum Distance
	public int minDistance = 0x000000400;
	
	//Loop Value
	public int loop = 1;
	
	//Delta Variables
	public int deltaBase = 9;
	public int deltaShift = 3;
	
	//Auto Flip
	public boolean autoFlip = true;
	
	//Scan Control
	public boolean scanControl = false;
	
	//Storage
	public int[] storage;
	
	public GraphicsState() {
		this.stack = new Stack<StackElement>();
	}
	
	public Number pop() {
		StackElement res = stack.peek();
		switch(res.type) {
		case 0:
			return popEInt8();
		case 1:
			return popEUInt16();
		case 2:
			return popEFWord();
		case 3:
			return popEF2Dot14();
		case 4:
			return popUInt32();
		case 5:
			return popInt32();
		case 6:
			return popF26Dot6();
		case 7:
		default:
			return res.value;
		}
	}
	
	public void push(int val, int type) {
		stack.push(new StackElement(val, type));
	}
	
	public long popUInt32() { 
		long res = stack.pop().value & 0x00000000FFFFFFFF;
		return res;
	}
	
	public int popInt32() {
		return stack.pop().value;
	}
	
	public int popEInt8() {
		int val = stack.pop().value;
		//If the number is negative.
		if(((val >> 7) & 1) == 1) {
			int res = 0xFFFFFF << 8;
			res += val;
			return res;
		}
		return val;
	}
	
	public int popEUInt16() {
		return stack.pop().value;
	}
	
	public int popEFWord() {
		int val = stack.pop().value;
		//If the number is negative.
		if(((val >> 15) & 1) == 1) {
			int res = 0xFFFF << 16;
			res += val;
			return res;
		}
		return val;
	}
	
	public int popEF2Dot14() {
		return ((Number) stack.pop().value).shortValue();
	}
	
	/*
	 * The 26.6 fixed point number is just an integer value divided by 64.
	 */
	public int popF26Dot6() {
		return stack.pop().value;
	}
}

class StackElement {
	
	int value;
	// 0: Eint8; 1: Euint16; 2: EFWord; 3: EF2Dot14; 4: uint32; 5: int32; 6: F26Dot6; 7: Stack Element
	int type;
	
	public StackElement(int value, int type) {
		this.value = value;
		this.type = type;
	}
}
