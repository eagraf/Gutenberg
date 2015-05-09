package graf.ethan.gutenberg.font;

import java.util.Stack;

/*
 * This class interprets instructions in TrueType fonts.
 */
public class TrueTypeInterpreter {
	
	private TableGlyph table;
	private GraphicsState state;
	
	public TrueTypeInterpreter(TableGlyph table) {
		this.table = table;
		this.state = new GraphicsState();
		
		//Execute every instruction.
		while(!table.instructions.isEmpty()) {
			execute();
		}
	}

	/*
	 * Push bytes from the instruction stream onto the stack.
	 */
	public void pushNBytes() {
		//The number of bytes to push.
		int num = (int) table.getInstruction();
		for(int i = 0; i < num; i ++) {
			int value = (int) table.getInstruction();
			state.push(value, 4);
		}
	}
	
	/*
	 * Push words from the instruction stream onto the stack.
	 */
	public void pushNWords() {
		//The number of words to push.
		int num = (int) table.getInstruction();
		for(int i = 0; i < num; i ++) {
			//Combine two bytes to form a word.
			int value = (table.getInstruction() << 8) + (int) table.getInstruction();
			state.push(value, 5);
		}
	}
	
	/*
	 * Push bytes to the stack.
	 */
	public void pushBytes(int num) {
		for(int i = 0; i < num; i ++) {
			int value = (int) table.getInstruction();
			state.push(value, 4);
		}
	}
	
	/*
	 * Push words to the stack.
	 */
	public void pushWords(int num) {
		for(int i = 0; i < num; i ++) {
			//Combine two bytes to form a word.
			int value = (table.getInstruction() << 8) + (int) table.getInstruction();
			state.push(value, 5);
		}
	}
	
	/*
	 * Copy an element at the specified index and push it to the top of the stack.
	 */
	public void copyIndex() {
		int index = (int) state.pop();
		StackElement element = state.stack.get(index);
		state.stack.push(element);
	}
	
	/*
	 * Remove an element from the specified index and push it to the top of the stack.
	 */
	public void moveIndex() {
		int index = (int) state.pop();
		StackElement element = state.stack.get(index);
		state.stack.removeElementAt(index);
		state.stack.push(element);
	}
	
	/*
	 * Move element 3 to the top of the stack.
	 */
	public void roll() {
		StackElement element = state.stack.get(2);
		state.stack.removeElementAt(2);
		state.stack.push(element);
	}
	
	/*
	 * Swap the first two elements of the stack.
	 */
	public void swap() {
		StackElement element = state.stack.get(1);
		state.stack.removeElementAt(1);
		state.stack.push(element);
	}
	
	/*
	 * Add the top two stack elements.
	 */
	public void add() {
		int n1 = state.popF26Dot6();
		int n2 = state.popF26Dot6();
		state.push(n1+n2, 6);
	}
	
	/*
	 * Subtract the top stack element from the element below it.
	 */
	public void subtract() {
		int n1 = state.popF26Dot6();
		int n2 = state.popF26Dot6();
		state.push(n2-n1, 6);
	}
	
	/*
	 * Multiply the top two stack elements.
	 */
	public void multiply() {
		long n1 = state.popF26Dot6();
		long n2 = state.popF26Dot6();
		int product = (int) ((n1 * n2) >> 6);
		state.push(product, 6);
	}
	
	/*
	 * Divide the top two stack elements.
	 */
	public void divide() {
		int n1 = state.popF26Dot6();
		int n2 = state.popF26Dot6();
		int quotient = ((n2 << 6)/n1);
		state.push(quotient, 6);
	}
	
	/*
	 * Push the absolute value of the top element.
	 */
	public void absoluteValue() {
		int n = state.popF26Dot6();
		state.push((int) Math.abs(n), 6);
	}
	
	/*
	 * Push the negation of the top element.
	 */
	public void negate() {
		int n = state.popF26Dot6();
		state.push(-(int) Math.abs(n), 6);
	}
	
	/*
	 * Push the floor value of the top element.
	 */
	public void floor() {
		int n = state.popF26Dot6();
		state.push(n / 64, 6);
	}
	
	/*
	 * Push the ceiling value of the top element.
	 */
	public void ceiling() {
		int n = state.popF26Dot6();
		state.push((n / 64) + 1, 6);
	}
	
	/*
	 * Push the larger of the top two stack elements.
	 */
	public void maximum() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 > e2) {
			state.push(e1, 6);
		}
		else {
			state.push(e2, 6);
		}
	}
	
	/*
	 * Push the lesser of the top two stack elements.
	 */
	public void minimum() {
		int e1 = state.popF26Dot6();
		int e2 = state.popF26Dot6();
		if(e1 < e2) {
			state.push(e1, 6);
		}
		else {
			state.push(e2, 6);
		}
	}
	
	public void lt() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() < e2.floatValue()) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	public void lteq() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() <= e2.floatValue()) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	public void gt() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() > e2.floatValue()) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	public void gteq() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() >= e2.floatValue()) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	public void eq() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() == e2.floatValue()) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	public void neq() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() != e2.floatValue()) {
			state.push(1,  4);
		}
		else {
			state.push(0, 4);
		}
	}
	
	public void odd() {
		
	}
	
	public void even() {
		
	}
	
	public void and() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() != 0f && e2.floatValue() != 0f) {
			state.push(1, 4);
		}
		else {
			state.push(0,  4);
		}
	}
	
	public void or() {
		Number e2 = state.pop();
		Number e1 = state.pop();
		if(e1.floatValue() != 0f || e2.floatValue() != 0f) {
			state.push(1, 4);
		}
		else {
			state.push(0,  4);
		}
	}
	
	public void not() {
		Number e2 = state.pop();
		if(e2.floatValue() != 0f) {
			state.push(0, 4);
		}
		else {
			state.push(1, 4);
		}
	}
	
	public void execute() {
		int code = (int) table.getInstruction();
		System.out.println(code);
		
		switch(code) {
		case 0x00:
		case 0x01:
			//SVTCA: Set Freedom & Prohection Vectors to Coordinate Axis
			break;
		case 0x02:
		case 0x03:
			//SPVTCA: Set Projection Vector to Coordinate Axis
			break;
		case 0x04:
		case 0x05:
			//SFVTCA: Set Freedom Vector to Coordinate Axis
			break;
		case 0x06:
		case 0x07:
			//SPVTL: Set Projection Vector to Line
			break;
		case 0x08:
		case 0x09:
			//SFVTL: Set Freedom Vector to Line
			break;
		case 0x0A:
			//SPVFS: Set Projection Vector From Stack
			break;
		case 0x0B:
			//SFVFS: Set Freedom Vector From Stack
			break;
		case 0x0C:
			//GPV: Get Projection Vector
			break;
		case 0x0E:
			//SFVTPV: Set Freedom Vector to Projection Vector
			break;
		case 0x0F:
			//ISECT: Move Point P to the Intersection of Two Lines
			break;
		case 0x0D:
			//GFV: Get Freedom Vector
			break;
		case 0x10:
			//SRP0: Set Reference Point 0
			break;
		case 0x11:
			//SRP0: Set Reference Point 1
			break;
		case 0x12:
			//SRP0: Set Reference Point 2
			break;
		case 0x13:
			//SZP0: Set Zone Pointer 0
			break;
		case 0x14:
			//SZP1: Set Zone Pointer 1
			break;
		case 0x15:
			//SZP2: Set Zone Pointer 2
			break;
		case 0x16:
			//SZPS: Set Zone Pointers
			break;
		case 0x17:
			//SLOOP: Set Loop Variable
			break;
		case 0x18:
			//RTG: Round to Grid
			break;
		case 0x19:
			//RTHG: Rount to Half Grid
			break;
		case 0x1A:
			//SMD: Set Minimum Distance
			break;
		case 0x1B:
			//ELSE: Else Clause
			break;
		case 0x1C:
			//JMPR: Jump Relative
			break;
		case 0x1D:
			//SCVTCI: Set Control Value Table Cut-In
			break;
		case 0x1E:
			//SSWCI: Set Single Width Cut-In
			break;
		case 0x1F:
			//SSW: Set Single Width
			break;
		case 0x20:
			//DUP: Duplicate the Top Stack Element
			state.stack.push(state.stack.peek());
			break;
		case 0x21:
			//POP: Pop Top Stack Element
			state.stack.pop();
			break;
		case 0x22:
			//CLEAR: Clear the Stack
			state.stack.clear();
			break;
		case 0x23:
			//SWAP: Swap the Top Two Elements on the Stack
			swap();
			break;
		case 0x24:
			//DEPTH: Depth of the Stack
			state.push(state.stack.size(), 5);
			break;
		case 0x25:
			//CINDEX: Copy the Indexed Element to the Top of the Stack
			copyIndex();
			break;
		case 0x26:
			//MINDEX: Move the Indexed Element to the Top of the Stack
			moveIndex();
			break;
		case 0x27:
			//ALIGNPTS: Align Points
			break;
		case 0x29:
			//UTP: Untouch Point
		case 0x2A:
			//LOOPCALL: Loop and Call Function
		case 0x2B:
			//CALL: Call Function
		case 0x2C:
			//FDEF: Function Definition
		case 0x2D:
			//ENDF: End Function Definition
		case 0x2E:
		case 0x2F:
			//MDAP: Move Direct Absolute Point
			break;
		case 0x30:
		case 0x31:
			//IUP: Interpolate Untouched Points Through the Outline
			break;
		case 0x32:
		case 0x33:
			//SHP: Shift Point Using Reference Point
		case 0x34:
		case 0x35:
			//SHC: Shift Contour Using Reference Point
		case 0x36:
		case 0x37:
			//SHZ: Shift Zone Using Reference Point
		case 0x38:
			//SHPIX: Shift Point by a Pixel Amount
		case 0x39:
			//IP: Interpolate Point
		case 0x3A:
		case 0x3B:
			//MSIRP: Move Stack Indirect Relative Point
			break;
		case 0x3C:
			//ALIGNRP: Align Reference Point
		case 0x3D:
			//RTDG: Round to Double Grid
		case 0x3E:
		case 0x3F:
			//MIAP: Move Indirect Absolute Point
			break;
		case 0x40:
			//NPUSHB: Push N Bytes
			pushNBytes();
			break;
		case 0x41:
			//NPUSHW: Push N Words
			pushNWords();
			break;
		case 0x42:
			//WS: Write Store
		case 0x43:
			//RS: Read Store
		case 0x44:
			//WCVTP: Write Control Value Table in Pixel Units
		case 0x45:
			//RCVT: Read Control Value Table Entry
		case 0x46: 
		case 0x47:
			//GC: Get Coordinate Projected Onto the Projection Vectior
			break;
		case 0x48:
			//SCFS: Set Coordinate From the Stack Using Projection & Freedom Vectors
		case 0x49:
		case 0x4A:
			//MD: Measure Distance
			break;
		case 0x4B:
			//MPPEM: Measure Pixels Per EM
		case 0x4D:
			//FLIPON: Set the Auto FLIP Boolean to On
		case 0x4E:
			//FLIPOFF: Set the Auto FLIP Boolean to Off
		case 0x4F:
			//DEBUG: Debug
		case 0x50:
			//LT: Less Than
			lt();
			break;
		case 0x51:
			//LTEQ: Less Than or Equal
			lteq();
			break;
		case 0x52:
			//GT: Greater Than
			gt();
			break;
		case 0x53:
			//GTEQ: Greater Than or Equal
			gteq();
			break;
		case 0x54:
			//EQ: Equal
			eq();
			break;
		case 0x55:
			//NEQ: Not Equal
			neq();
			break;
		case 0x56:
			//ODD: Odd
		case 0x57:
			//EVEN: Even
		case 0x58:
			//IF: If Test
		case 0x59:
			//EIF: End IF
		case 0x5A:
			//AND: Logical And
			and();
			break;
		case 0x5B:
			//OR: Logical Or
			or();
			break;
		case 0x5C:
			//NOT: Logical Not
			not();
			break;
		case 0x5D:
			//DELTAP1: Delta Exception P1
		case 0x5E:
			//SDB: Set Delta Base in the Graphics State
		case 0x5F:
			//SDS: Set Delta Shift in the Graphics State
		case 0x60:
			//ADD: Add
			add();
			break;
		case 0x61:
			//SUB: Subtract
			subtract();
			break;
		case 0x62:
			//DIV: Divide
			divide();
			break;
		case 0x63:
			//MUL: Multiply
			multiply();
			break;
		case 0x64:
			//ABS: Absolute Value
			absoluteValue();
			break;
		case 0x65:
			//NEG: Negate
			negate();
			break;
		case 0x66:
			//FLOOR: Floor
			floor();
			break;
		case 0x67:
			//CEILING: Ceiling
			ceiling();
			break;
		case 0x68:
		case 0x69:
		case 0x6A:
		case 0x6B:
			//ROUND: Round Value
		case 0x6C:
		case 0x6D:
		case 0x6E:
		case 0x6F:
			//NROUND: No Rounding of Value
			break;
		case 0x70:
			//WCVTF: Write Control Value Table in Funits
		case 0x71:
			//DELTAP2: Delta Exception P2
		case 0x72:
			//DELTAP3: Delta Exception P3
		case 0x73:
			//DELTAC1: Delta Exception C1
		case 0x74:
			//DELTAC2: Delta Exception C2
		case 0x75:
			//DELTAC3: Delta Exception C3
		case 0x76:
			//SROUNDL Super Round
		case 0x77:
			//S45ROUND: Super ROund 45 Degrees
		case 0x78:
			//JROT: Jump Relative on True
		case 0x79:
			//JROF: Jump Relative on False
		case 0x7A:
			//ROFF: Round Off
		case 0x7C:
			//RUTG: Round Up to Grid
		case 0x7D:
			//RDTG: Round Down to Grid
		case 0x7E:
			//SANGW: Set Angle Weight
		case 0x7F:
			//AA: Adjust Angle
		case 0x80:
			//FLIPPT: Flip Point
		case 0x81:
			//FLIPRGON: Flip Range On
		case 0x82:
			//FLIPRGOFF: Flip Range Off
		case 0x85:
			//SCANCTRL: Scan Conversion Control
		case 0x86:
		case 0x87:
			//SDPVTL: Set Dual Projection Vector to Line
			break;
		case 0x88:
			//GETINFO: Get Information
		case 0x89:
			//IDEF: Instruction Definition
			break;
		case 0x8A:
			//ROLL: Roll the Top Three Stack Elements
			roll();
			break;
		case 0x8B:
			//MAX: Maximum of Top Two Stack Elements
			maximum();
			break;
		case 0x8C:
			//MIN: Maximum of Top Two Stack Elements
			minimum();
			break;
		case 0x8D:
			//SCANTYPE: Scantype
		case 0x8E:
			//INSTCTRL: Instruction Execution Control
		case 0xB0:
		case 0xB1:
		case 0xB2:
		case 0xB3:
		case 0xB4:
		case 0xB5:
		case 0xB6:
		case 0xB7:
			//PUSHB: Push Bytes
			pushBytes(code - 0xB0 + 1);
			break;
		case 0xB8:
		case 0xB9:
		case 0xBA:
		case 0xBB:
		case 0xBC:
		case 0xBD:
		case 0xBE:
		case 0xBF:
			//PUSHW: Push Words
			pushWords(code - 0xB8 + 1);
			break;
		case 0xC0:
		case 0xC1:
		case 0xC2:
		case 0xC3:
		case 0xC4:
		case 0xC5:
		case 0xC6:
		case 0xC7:
		case 0xC8:
		case 0xC9:
		case 0xCA:
		case 0xCB:
		case 0xCC:
		case 0xCD:
		case 0xCE:
		case 0xCF:
		case 0xD0:
		case 0xD1:
		case 0xD2:
		case 0xD3:
		case 0xD4:
		case 0xD5:
		case 0xD6:
		case 0xD7:
		case 0xD8:
		case 0xD9:
		case 0xDA:
		case 0xDB:
		case 0xDC:
		case 0xDD:
		case 0xDE:
		case 0xDF:
			//MDRP: Move Direct Relative Point
		case 0xE0:
		case 0xE1:
		case 0xE2:
		case 0xE3:
		case 0xE4:
		case 0xE5:
		case 0xE6:
		case 0xE7:
		case 0xE8:
		case 0xE9:
		case 0xEA:
		case 0xEB:
		case 0xEC:
		case 0xED:
		case 0xEE:
		case 0xEF:
		case 0xF0:
		case 0xF1:
		case 0xF2:
		case 0xF3:
		case 0xF4:
		case 0xF5:
		case 0xF6:
		case 0xF7:
		case 0xF8:
		case 0xF9:
		case 0xFA:
		case 0xFB:
		case 0xFC:
		case 0xFD:
		case 0xFE:
		case 0xFF:
			//MDRP: Move Direct Relative Point
		}
	}

}

/*
 * The graphics state. Begins with default values, does not transfer between glyphs.
 */
class GraphicsState {
	
	public Stack<StackElement> stack;
	
	public GraphicsState() {
		this.stack = new Stack<StackElement>();
	}
	
	public Number pop() {
		StackElement res = stack.pop();
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
			return stack.pop().value;
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
	
	public float popEF2Dot14() {
		int val = stack.pop().value;
		if(((val >> 15) & 1) == 1) {
			int num = 0xFFFF << 16;
			num += val;
			return num / 16384f;
		}
		return val / 16384f;
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

